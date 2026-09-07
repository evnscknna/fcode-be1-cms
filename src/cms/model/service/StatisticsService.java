package cms.model.service;

import cms.model.dao.CourseDao;
import cms.model.dao.EnrollmentDao;
import cms.model.dao.WaitlistDao;
import cms.model.dto.Course;
import cms.model.dto.Enrollment;
import cms.model.dto.WaitlistEntry;
import cms.model.report.CourseStatistics;
import cms.model.report.OverallStatistics;
import java.util.ArrayList;
import java.util.List;

public class StatisticsService {

    private final CourseDao courseDao;
    private final EnrollmentDao enrollmentDao;
    private final WaitlistDao waitlistDao;

    public StatisticsService(CourseDao courseDao, EnrollmentDao enrollmentDao, WaitlistDao waitlistDao) {
        this.courseDao = courseDao;
        this.enrollmentDao = enrollmentDao;
        this.waitlistDao = waitlistDao;
    }

    public CourseStatistics forCourse(int courseId) {
        return build(courseDao.getById(courseId));
    }

    public List<CourseStatistics> forAllCourses() {
        List<CourseStatistics> out = new ArrayList<>();
        for (Course c : courseDao.findAll()) {
            out.add(build(c));
        }
        return out;
    }

    public OverallStatistics overall() {
        List<CourseStatistics> stats = forAllCourses();
        int totalActive = 0;
        double fillSum = 0.0;
        CourseStatistics most = null;
        CourseStatistics least = null;
        for (CourseStatistics s : stats) {
            totalActive += s.getEnrolled();
            fillSum += s.getFillRatePct();
            if (most == null || s.getEnrolled() > most.getEnrolled()) {
                most = s;
            }
            if (least == null || s.getEnrolled() < least.getEnrolled()) {
                least = s;
            }
        }
        double avgFill = stats.isEmpty() ? 0.0 : fillSum / stats.size();
        return new OverallStatistics(
                stats.size(),
                totalActive,
                waitlistDao.count(),
                avgFill,
                most == null ? "-" : most.getCode() + " - " + most.getTitle(),
                most == null ? 0 : most.getEnrolled(),
                least == null ? "-" : least.getCode() + " - " + least.getTitle(),
                least == null ? 0 : least.getEnrolled());
    }

    private CourseStatistics build(Course c) {
        int enrolled = 0;
        int withdrawn = 0;
        int completed = 0;
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getCourseId() != c.getId()) {
                continue;
            }
            switch (e.getStatus()) {
                case ENROLLED:
                    enrolled++;
                    break;
                case WITHDRAWN:
                    withdrawn++;
                    break;
                case COMPLETED:
                    completed++;
                    break;
                default:
                    break;
            }
        }
        int waiting = 0;
        for (WaitlistEntry w : waitlistDao.findAll()) {
            if (w.getCourseId() == c.getId()) {
                waiting++;
            }
        }
        return new CourseStatistics(c.getId(), c.getCode(), c.getTitle(), c.getCapacity(),
                enrolled, waiting, withdrawn, completed);
    }
}
