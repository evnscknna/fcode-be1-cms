package cms.model.service;

import cms.model.dao.CourseDao;
import cms.model.dao.EnrollmentDao;
import cms.model.dao.InstructorDao;
import cms.model.dto.Course;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.model.service.history.OperationHistory;
import cms.util.SearchUtils;
import cms.util.SortUtils;
import cms.util.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CourseService {

    private static final int MIN_CREDITS = 1;
    private static final int MAX_CREDITS = 12;
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 500;

    private final CourseDao courseDao;
    private final InstructorDao instructorDao;
    private final EnrollmentDao enrollmentDao;
    private final RegistrationRuleService ruleService;
    private final WaitlistService waitlistService;
    private final OperationHistory history;

    public CourseService(CourseDao courseDao, InstructorDao instructorDao, EnrollmentDao enrollmentDao,
                         RegistrationRuleService ruleService, WaitlistService waitlistService,
                         OperationHistory history) {
        this.courseDao = courseDao;
        this.instructorDao = instructorDao;
        this.enrollmentDao = enrollmentDao;
        this.ruleService = ruleService;
        this.waitlistService = waitlistService;
        this.history = history;
    }

    public List<Course> listAll() {
        return courseDao.findAll();
    }

    public Course get(int id) {
        return courseDao.getById(id);
    }

    public Course create(Course draft) {
        normalizeAndValidate(draft, 0);
        ruleService.validatePrerequisiteGraph(draft, courseDao.findAll());
        Course course = new Course(0, draft.getCode(), draft.getTitle(), draft.getCredits(),
                draft.getCapacity(), draft.getSemester(), draft.getInstructorId());
        course.setSchedule(draft.getSchedule());
        course.setPrerequisites(draft.getPrerequisites());
        return courseDao.insert(course);
    }

    // Returns how many students were auto-enrolled from the waiting list.
    public int update(int id, Course draft) {
        Course existing = courseDao.getById(id);
        normalizeAndValidate(draft, id);

        int oldCapacity = existing.getCapacity();
        existing.setCode(draft.getCode());
        existing.setTitle(draft.getTitle());
        existing.setCredits(draft.getCredits());
        existing.setCapacity(draft.getCapacity());
        existing.setSemester(draft.getSemester());
        existing.setInstructorId(draft.getInstructorId());
        existing.setSchedule(draft.getSchedule());
        existing.setPrerequisites(draft.getPrerequisites());

        ruleService.validatePrerequisiteGraph(existing, courseDao.findAll());
        courseDao.update(existing);

        if (existing.getCapacity() > oldCapacity) {
            int promoted = waitlistService.promoteToFill(existing);
            if (promoted > 0) {
                history.clear(); // auto-fill is not undoable
            }
            return promoted;
        }
        return 0;
    }

    public void delete(int id) {
        Course course = courseDao.getById(id);
        if (hasActiveEnrollments(id)) {
            throw new IllegalStateException("Cannot delete " + course.getCode()
                    + ": students are enrolled. Withdraw them first.");
        }
        for (Course other : courseDao.findAll()) {
            if (other.getId() != id && containsIgnoreCase(other.getPrerequisites(), course.getCode())) {
                throw new IllegalStateException("Cannot delete " + course.getCode()
                        + ": it is a prerequisite for " + other.getCode() + ".");
            }
        }
        waitlistService.clearCourse(id);
        courseDao.deleteById(id);
        history.clear();
    }

    public List<Course> search(String query) {
        return SearchUtils.filterByText(courseDao.findAll(), query,
                c -> c.getCode() + " " + c.getTitle() + " " + c.getSemester());
    }

    public List<Course> sortedBy(String key) {
        Comparator<Course> cmp;
        switch (key == null ? "" : key.trim().toLowerCase()) {
            case "title":
                cmp = Comparator.comparing(Course::getTitle, String.CASE_INSENSITIVE_ORDER);
                break;
            case "credits":
                cmp = Comparator.comparingInt(Course::getCredits)
                        .thenComparing(Course::getCode, String.CASE_INSENSITIVE_ORDER);
                break;
            case "capacity":
                cmp = Comparator.comparingInt(Course::getCapacity)
                        .thenComparing(Course::getCode, String.CASE_INSENSITIVE_ORDER);
                break;
            case "code":
            case "":
                cmp = Comparator.comparing(Course::getCode, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                throw new IllegalArgumentException("Unknown sort key: " + key);
        }
        return SortUtils.sorted(courseDao.findAll(), cmp);
    }

    private void normalizeAndValidate(Course draft, int selfId) {
        draft.setCode(Validator.requireMaxLength(draft.getCode(), 12, "Course code").toUpperCase());
        draft.setTitle(Validator.requireMaxLength(draft.getTitle(), 100, "Title"));
        draft.setSemester(Validator.requireMaxLength(draft.getSemester(), 20, "Semester").toUpperCase());
        Validator.requireRange(draft.getCredits(), MIN_CREDITS, MAX_CREDITS, "Credits");
        Validator.requireRange(draft.getCapacity(), MIN_CAPACITY, MAX_CAPACITY, "Capacity");

        if (draft.getInstructorId() > 0 && !instructorDao.existsById(draft.getInstructorId())) {
            throw new IllegalArgumentException("Instructor not found: " + draft.getInstructorId());
        }
        for (Course c : courseDao.findAll()) {
            if (c.getId() != selfId && c.getCode().equalsIgnoreCase(draft.getCode())) {
                throw new IllegalArgumentException("A course with code " + draft.getCode()
                        + " already exists.");
            }
        }
        Set<String> prereqs = new LinkedHashSet<>();
        for (String code : draft.getPrerequisites()) {
            String c = code == null ? "" : code.trim().toUpperCase();
            if (!c.isEmpty()) {
                prereqs.add(c);
            }
        }
        draft.setPrerequisites(new ArrayList<>(prereqs));
    }

    private boolean hasActiveEnrollments(int courseId) {
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getCourseId() == courseId && e.getStatus() == EnrollmentStatus.ENROLLED) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        for (String s : list) {
            if (s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
