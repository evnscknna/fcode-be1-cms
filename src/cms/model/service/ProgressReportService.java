package cms.model.service;

import cms.model.dao.CourseDao;
import cms.model.dao.EnrollmentDao;
import cms.model.dao.StudentDao;
import cms.model.dto.Course;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.model.dto.Student;
import cms.model.report.ProgressReport;
import cms.util.SortUtils;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProgressReportService {

    public static final int TARGET_TOTAL_CREDITS = 120;

    private static final Map<String, Double> GRADE_POINTS = new HashMap<>();

    static {
        GRADE_POINTS.put("A", 4.0);
        GRADE_POINTS.put("A-", 3.7);
        GRADE_POINTS.put("B+", 3.3);
        GRADE_POINTS.put("B", 3.0);
        GRADE_POINTS.put("B-", 2.7);
        GRADE_POINTS.put("C+", 2.3);
        GRADE_POINTS.put("C", 2.0);
        GRADE_POINTS.put("C-", 1.7);
        GRADE_POINTS.put("D+", 1.3);
        GRADE_POINTS.put("D", 1.0);
        GRADE_POINTS.put("F", 0.0);
    }

    private final EnrollmentDao enrollmentDao;
    private final CourseDao courseDao;
    private final StudentDao studentDao;

    public ProgressReportService(EnrollmentDao enrollmentDao, CourseDao courseDao, StudentDao studentDao) {
        this.enrollmentDao = enrollmentDao;
        this.courseDao = courseDao;
        this.studentDao = studentDao;
    }

    public ProgressReport forStudent(int studentId) {
        Student student = studentDao.getById(studentId);

        int completedCredits = 0;
        int inProgressCredits = 0;
        double qualityPoints = 0.0;
        int gradedCredits = 0;

        Map<String, ProgressReport.SemesterBlock> bySemester = new TreeMap<>();

        List<Enrollment> rows = SortUtils.sorted(enrollmentDao.findAll(),
                Comparator.comparingLong(Enrollment::getTimestamp));

        for (Enrollment e : rows) {
            if (e.getStudentId() != studentId) {
                continue;
            }
            Course course = courseDao.findById(e.getCourseId());
            String code = course != null ? course.getCode() : "#" + e.getCourseId();
            String title = course != null ? course.getTitle() : "(deleted course)";
            int credits = course != null ? course.getCredits() : 0;
            String grade = e.getGrade() == null ? "" : e.getGrade().trim();

            if (e.getStatus() == EnrollmentStatus.COMPLETED) {
                completedCredits += credits;
                Double points = GRADE_POINTS.get(grade.toUpperCase());
                if (points != null) {
                    qualityPoints += points * credits;
                    gradedCredits += credits;
                }
            } else if (e.getStatus() == EnrollmentStatus.ENROLLED) {
                inProgressCredits += credits;
            }

            String semester = e.getSemester() == null || e.getSemester().isEmpty()
                    ? "(unspecified)" : e.getSemester();
            bySemester.computeIfAbsent(semester, ProgressReport.SemesterBlock::new)
                    .getLines().add(new ProgressReport.Line(code, title, credits,
                            e.getStatus().name(), grade));
        }

        double gpa = gradedCredits == 0 ? -1.0 : qualityPoints / gradedCredits;

        return new ProgressReport(studentId, student.getFullName(), completedCredits,
                inProgressCredits, TARGET_TOTAL_CREDITS, gpa,
                new java.util.ArrayList<>(bySemester.values()));
    }
}
