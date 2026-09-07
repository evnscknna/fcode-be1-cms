package cms.model.service;

import cms.model.dao.CourseDao;
import cms.model.dao.EnrollmentDao;
import cms.model.dto.Course;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.model.dto.Student;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RegistrationRuleService {

    public static final int MAX_CREDITS_PER_SEMESTER = 24;

    private final EnrollmentDao enrollmentDao;
    private final CourseDao courseDao;

    public RegistrationRuleService(EnrollmentDao enrollmentDao, CourseDao courseDao) {
        this.enrollmentDao = enrollmentDao;
        this.courseDao = courseDao;
    }

    public void validateEnrollment(Student student, Course course) {
        checkNotDuplicate(student, course);
        checkPrerequisites(student, course);
        checkCreditLimit(student, course);
    }

    public void checkNotDuplicate(Student student, Course course) {
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getStudentId() != student.getId() || e.getCourseId() != course.getId()) {
                continue;
            }
            if (e.getStatus() == EnrollmentStatus.ENROLLED) {
                throw new IllegalArgumentException(
                        student.getFullName() + " is already enrolled in " + course.getCode() + ".");
            }
            if (e.getStatus() == EnrollmentStatus.COMPLETED) {
                throw new IllegalArgumentException(
                        student.getFullName() + " has already completed " + course.getCode() + ".");
            }
        }
    }

    public void checkPrerequisites(Student student, Course course) {
        if (course.getPrerequisites().isEmpty()) {
            return;
        }
        Set<String> completed = new HashSet<>();
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getStudentId() == student.getId() && e.getStatus() == EnrollmentStatus.COMPLETED) {
                Course done = courseDao.findById(e.getCourseId());
                if (done != null) {
                    completed.add(done.getCode().toUpperCase());
                }
            }
        }
        List<String> missing = new ArrayList<>();
        for (String req : course.getPrerequisites()) {
            if (!completed.contains(req.toUpperCase())) {
                missing.add(req);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(student.getFullName()
                    + " has not completed prerequisite(s): " + String.join(", ", missing));
        }
    }

    public void checkCreditLimit(Student student, Course course) {
        int current = 0;
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getStudentId() == student.getId()
                    && e.getStatus() == EnrollmentStatus.ENROLLED
                    && Objects.equals(e.getSemester(), course.getSemester())) {
                Course c = courseDao.findById(e.getCourseId());
                if (c != null) {
                    current += c.getCredits();
                }
            }
        }
        if (current + course.getCredits() > MAX_CREDITS_PER_SEMESTER) {
            throw new IllegalArgumentException("Enrolling in " + course.getCode()
                    + " (" + course.getCredits() + " cr) would go over the "
                    + MAX_CREDITS_PER_SEMESTER + "-credit limit for " + course.getSemester()
                    + " (currently " + current + " cr).");
        }
    }

    public void validatePrerequisiteGraph(Course candidate, List<Course> allCourses) {
        String candidateCode = candidate.getCode().toUpperCase();
        Map<String, List<String>> graph = new HashMap<>();
        for (Course c : allCourses) {
            graph.put(c.getCode().toUpperCase(), upper(c.getPrerequisites()));
        }
        graph.put(candidateCode, upper(candidate.getPrerequisites()));

        for (String req : candidate.getPrerequisites()) {
            String reqUpper = req.toUpperCase();
            if (reqUpper.equals(candidateCode)) {
                throw new IllegalArgumentException("A course cannot be its own prerequisite.");
            }
            if (!graph.containsKey(reqUpper)) {
                throw new IllegalArgumentException("Prerequisite course '" + req + "' does not exist.");
            }
        }
        if (hasCycleFrom(candidateCode, graph, new HashMap<>())) {
            throw new IllegalArgumentException("Prerequisites for " + candidate.getCode()
                    + " would form a loop.");
        }
    }

    // DFS. colour: 0 unseen, 1 on current path, 2 done. Hitting a 1 means a loop.
    private boolean hasCycleFrom(String node, Map<String, List<String>> graph, Map<String, Integer> colour) {
        colour.put(node, 1);
        for (String next : graph.getOrDefault(node, new ArrayList<>())) {
            Integer c = colour.get(next);
            if (c == null || c == 0) {
                if (hasCycleFrom(next, graph, colour)) {
                    return true;
                }
            } else if (c == 1) {
                return true;
            }
        }
        colour.put(node, 2);
        return false;
    }

    private static List<String> upper(List<String> codes) {
        List<String> out = new ArrayList<>();
        for (String s : codes) {
            out.add(s.toUpperCase());
        }
        return out;
    }
}
