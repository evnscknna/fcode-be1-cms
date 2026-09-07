package cms.model.service;

import cms.model.dao.EnrollmentDao;
import cms.model.dao.StudentDao;
import cms.model.dao.WaitlistDao;
import cms.model.dto.Course;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.model.dto.Student;
import cms.model.dto.WaitlistEntry;
import cms.model.service.history.AddEnrollmentCommand;
import cms.model.service.history.AddWaitlistCommand;
import cms.model.service.history.Command;
import cms.model.service.history.RemoveWaitlistCommand;
import cms.util.SortUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WaitlistService {

    private final WaitlistDao waitlistDao;
    private final EnrollmentDao enrollmentDao;
    private final StudentDao studentDao;
    private final RegistrationRuleService ruleService;
    private final ScheduleService scheduleService;

    public WaitlistService(WaitlistDao waitlistDao, EnrollmentDao enrollmentDao, StudentDao studentDao,
                           RegistrationRuleService ruleService, ScheduleService scheduleService) {
        this.waitlistDao = waitlistDao;
        this.enrollmentDao = enrollmentDao;
        this.studentDao = studentDao;
        this.ruleService = ruleService;
        this.scheduleService = scheduleService;
    }

    public int activeEnrolledCount(int courseId) {
        int n = 0;
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getCourseId() == courseId && e.getStatus() == EnrollmentStatus.ENROLLED) {
                n++;
            }
        }
        return n;
    }

    public List<WaitlistEntry> queueFor(int courseId) {
        List<WaitlistEntry> queue = new ArrayList<>();
        for (WaitlistEntry w : waitlistDao.findAll()) {
            if (w.getCourseId() == courseId) {
                queue.add(w);
            }
        }
        SortUtils.mergeSort(queue, Comparator
                .comparingLong(WaitlistEntry::getTimestamp)
                .thenComparingInt(WaitlistEntry::getId));
        return queue;
    }

    public boolean isWaitlisted(int studentId, int courseId) {
        return findEntry(studentId, courseId) != null;
    }

    public WaitlistEntry findEntry(int studentId, int courseId) {
        for (WaitlistEntry w : waitlistDao.findAll()) {
            if (w.getStudentId() == studentId && w.getCourseId() == courseId) {
                return w;
            }
        }
        return null;
    }

    public int positionOf(int studentId, int courseId) {
        List<WaitlistEntry> queue = queueFor(courseId);
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getStudentId() == studentId) {
                return i + 1;
            }
        }
        return -1;
    }

    public AddWaitlistCommand buildAddToWaitlist(int studentId, int courseId) {
        if (isWaitlisted(studentId, courseId)) {
            throw new IllegalStateException("That student is already on the waiting list for this course.");
        }
        WaitlistEntry entry = new WaitlistEntry(0, studentId, courseId, System.currentTimeMillis());
        return new AddWaitlistCommand(waitlistDao, entry);
    }

    // Plan how to fill freeSeats from the queue, oldest first, skipping anyone
    // who fails a rule. The caller runs the commands (now, or via undo history).
    public List<Command> planPromotion(Course course, int freeSeats) {
        List<Command> plan = new ArrayList<>();
        int remaining = freeSeats;
        for (WaitlistEntry entry : queueFor(course.getId())) {
            if (remaining <= 0) {
                break;
            }
            Student student = studentDao.findById(entry.getStudentId());
            if (student == null) {
                plan.add(new RemoveWaitlistCommand(waitlistDao, entry));
                continue;
            }
            try {
                ruleService.validateEnrollment(student, course);
                scheduleService.assertNoConflict(student.getId(), course);
            } catch (IllegalArgumentException notEligible) {
                continue;
            }
            plan.add(new RemoveWaitlistCommand(waitlistDao, entry));
            plan.add(new AddEnrollmentCommand(enrollmentDao, new Enrollment(0, student.getId(),
                    course.getId(), course.getSemester(), EnrollmentStatus.ENROLLED, "",
                    System.currentTimeMillis())));
            remaining--;
        }
        return plan;
    }

    public int promoteToFill(Course course) {
        int freeSeats = Math.max(0, course.getCapacity() - activeEnrolledCount(course.getId()));
        List<Command> plan = planPromotion(course, freeSeats);
        int promoted = 0;
        for (Command c : plan) {
            c.execute();
            if (c instanceof AddEnrollmentCommand) {
                promoted++;
            }
        }
        return promoted;
    }

    public void removeFromWaitlist(int studentId, int courseId) {
        WaitlistEntry entry = findEntry(studentId, courseId);
        if (entry == null) {
            throw new IllegalArgumentException(
                    "That student is not on the waiting list for this course.");
        }
        waitlistDao.deleteById(entry.getId());
    }

    public void clearCourse(int courseId) {
        for (WaitlistEntry w : waitlistDao.findAll()) {
            if (w.getCourseId() == courseId) {
                waitlistDao.deleteById(w.getId());
            }
        }
    }
}
