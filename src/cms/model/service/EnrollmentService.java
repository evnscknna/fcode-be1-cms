package cms.model.service;

import cms.model.dao.CourseDao;
import cms.model.dao.EnrollmentDao;
import cms.model.dao.StudentDao;
import cms.model.dao.WaitlistDao;
import cms.model.dto.Course;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.model.dto.Student;
import cms.model.dto.WaitlistEntry;
import cms.model.service.history.AddEnrollmentCommand;
import cms.model.service.history.Command;
import cms.model.service.history.CompositeCommand;
import cms.model.service.history.OperationHistory;
import cms.model.service.history.RemoveWaitlistCommand;
import cms.model.service.history.SetEnrollmentStatusCommand;
import cms.util.Validator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EnrollmentService {

    private final EnrollmentDao enrollmentDao;
    private final WaitlistDao waitlistDao;
    private final CourseDao courseDao;
    private final StudentDao studentDao;
    private final RegistrationRuleService ruleService;
    private final ScheduleService scheduleService;
    private final WaitlistService waitlistService;
    private final OperationHistory history;

    public EnrollmentService(EnrollmentDao enrollmentDao, WaitlistDao waitlistDao, CourseDao courseDao,
                             StudentDao studentDao, RegistrationRuleService ruleService,
                             ScheduleService scheduleService, WaitlistService waitlistService,
                             OperationHistory history) {
        this.enrollmentDao = enrollmentDao;
        this.waitlistDao = waitlistDao;
        this.courseDao = courseDao;
        this.studentDao = studentDao;
        this.ruleService = ruleService;
        this.scheduleService = scheduleService;
        this.waitlistService = waitlistService;
        this.history = history;
    }

    // Returns false if the course is full (a broken rule still throws).
    public boolean enroll(int studentId, int courseId) {
        Student student = studentDao.getById(studentId);
        Course course = courseDao.getById(courseId);

        ruleService.validateEnrollment(student, course);
        scheduleService.assertNoConflict(studentId, course);

        if (waitlistService.activeEnrolledCount(courseId) >= course.getCapacity()) {
            return false;
        }

        List<Command> steps = new ArrayList<>();
        WaitlistEntry queued = waitlistService.findEntry(studentId, courseId);
        if (queued != null) {
            steps.add(new RemoveWaitlistCommand(waitlistDao, queued));
        }
        steps.add(new AddEnrollmentCommand(enrollmentDao, new Enrollment(0, studentId, courseId,
                course.getSemester(), EnrollmentStatus.ENROLLED, "", System.currentTimeMillis())));

        history.perform(new CompositeCommand(
                "Enroll " + student.getCode() + " in " + course.getCode(), steps));
        return true;
    }

    public void joinWaitlist(int studentId, int courseId) {
        Student student = studentDao.getById(studentId);
        Course course = courseDao.getById(courseId);

        if (waitlistService.activeEnrolledCount(courseId) < course.getCapacity()) {
            throw new IllegalStateException(course.getCode()
                    + " has free seats - enroll the student directly.");
        }
        ruleService.checkNotDuplicate(student, course);

        Command add = waitlistService.buildAddToWaitlist(studentId, courseId);
        history.perform(new CompositeCommand(
                "Wait-list " + student.getCode() + " for " + course.getCode(),
                Collections.singletonList(add)));
    }

    // Returns how many students were pulled off the waiting list.
    public int withdraw(int studentId, int courseId) {
        Student student = studentDao.getById(studentId);
        Course course = courseDao.getById(courseId);

        Enrollment active = findActiveEnrollment(studentId, courseId);
        if (active == null) {
            throw new IllegalStateException(student.getCode() + " is not currently enrolled in "
                    + course.getCode() + ".");
        }

        List<Command> steps = new ArrayList<>();
        steps.add(new SetEnrollmentStatusCommand(enrollmentDao, active.getId(),
                EnrollmentStatus.ENROLLED, EnrollmentStatus.WITHDRAWN));

        int activeCount = waitlistService.activeEnrolledCount(courseId);
        int freeSeatsAfter = Math.max(0, course.getCapacity() - (activeCount - 1)); // -1: withdrawal not run yet
        List<Command> promotions = waitlistService.planPromotion(course, freeSeatsAfter);
        steps.addAll(promotions);

        history.perform(new CompositeCommand(
                "Withdraw " + student.getCode() + " from " + course.getCode(), steps));

        return countEnrollments(promotions);
    }

    // Admin fix, not undoable. Frees a seat, so fill the queue and drop the history.
    public void recordCompletion(int studentId, int courseId, String grade) {
        String g = Validator.requireText(grade, "Grade").toUpperCase();
        Enrollment active = findActiveEnrollment(studentId, courseId);
        if (active == null) {
            throw new IllegalStateException("No active enrollment for that student and course.");
        }
        active.setStatus(EnrollmentStatus.COMPLETED);
        active.setGrade(g);
        active.setTimestamp(System.currentTimeMillis());
        enrollmentDao.update(active);
        waitlistService.promoteToFill(courseDao.getById(courseId));
        history.clear();
    }

    public String undo() {
        return history.undo();
    }

    public String redo() {
        return history.redo();
    }

    public List<String> undoHistory() {
        return history.undoHistory();
    }

    public List<String> redoHistory() {
        return history.redoHistory();
    }

    public List<Student> enrolledStudents(int courseId) {
        List<Student> out = new ArrayList<>();
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getCourseId() == courseId && e.getStatus() == EnrollmentStatus.ENROLLED) {
                Student s = studentDao.findById(e.getStudentId());
                if (s != null) {
                    out.add(s);
                }
            }
        }
        return out;
    }

    private Enrollment findActiveEnrollment(int studentId, int courseId) {
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getStudentId() == studentId && e.getCourseId() == courseId
                    && e.getStatus() == EnrollmentStatus.ENROLLED) {
                return e;
            }
        }
        return null;
    }

    private static int countEnrollments(List<Command> commands) {
        int n = 0;
        for (Command c : commands) {
            if (c instanceof AddEnrollmentCommand) {
                n++;
            }
        }
        return n;
    }
}
