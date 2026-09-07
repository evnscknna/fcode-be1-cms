package cms.model.service;

import cms.model.dao.EnrollmentDao;
import cms.model.dao.StudentDao;
import cms.model.dao.WaitlistDao;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.model.dto.Student;
import cms.model.dto.WaitlistEntry;
import cms.model.service.history.OperationHistory;
import cms.util.SearchUtils;
import cms.util.SortUtils;
import cms.util.Validator;
import java.util.Comparator;
import java.util.List;

public class StudentService {

    private static final int MIN_YEAR = 1980;

    private final StudentDao studentDao;
    private final EnrollmentDao enrollmentDao;
    private final WaitlistDao waitlistDao;
    private final OperationHistory history;

    public StudentService(StudentDao studentDao, EnrollmentDao enrollmentDao, WaitlistDao waitlistDao,
                          OperationHistory history) {
        this.studentDao = studentDao;
        this.enrollmentDao = enrollmentDao;
        this.waitlistDao = waitlistDao;
        this.history = history;
    }

    public List<Student> listAll() {
        return studentDao.findAll();
    }

    public Student get(int id) {
        return studentDao.getById(id);
    }

    public Student create(Student draft) {
        normalizeAndValidate(draft, 0);
        Student s = new Student(0, draft.getCode(), draft.getFullName(), draft.getEmail(),
                draft.getMajor(), draft.getEnrollmentYear());
        return studentDao.insert(s);
    }

    public void update(int id, Student draft) {
        Student existing = studentDao.getById(id);
        normalizeAndValidate(draft, id);
        existing.setCode(draft.getCode());
        existing.setFullName(draft.getFullName());
        existing.setEmail(draft.getEmail());
        existing.setMajor(draft.getMajor());
        existing.setEnrollmentYear(draft.getEnrollmentYear());
        studentDao.update(existing);
    }

    public void delete(int id) {
        Student student = studentDao.getById(id);
        if (hasActiveEnrollments(id)) {
            throw new IllegalStateException("Cannot delete " + student.getCode()
                    + ": the student has active enrollments. Withdraw them first.");
        }
        for (WaitlistEntry w : waitlistDao.findAll()) {
            if (w.getStudentId() == id) {
                waitlistDao.deleteById(w.getId());
            }
        }
        studentDao.deleteById(id);
        history.clear();
    }

    public List<Student> search(String query) {
        return SearchUtils.filterByText(studentDao.findAll(), query,
                s -> s.getCode() + " " + s.getFullName() + " " + s.getEmail() + " " + s.getMajor());
    }

    public List<Student> sortedBy(String key) {
        Comparator<Student> cmp;
        switch (key == null ? "" : key.trim().toLowerCase()) {
            case "code":
                cmp = Comparator.comparing(Student::getCode, String.CASE_INSENSITIVE_ORDER);
                break;
            case "year":
                cmp = Comparator.comparingInt(Student::getEnrollmentYear)
                        .thenComparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "name":
            case "":
                cmp = Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                throw new IllegalArgumentException("Unknown sort key: " + key);
        }
        return SortUtils.sorted(studentDao.findAll(), cmp);
    }

    private void normalizeAndValidate(Student draft, int selfId) {
        draft.setCode(Validator.requireMaxLength(draft.getCode(), 12, "Student code").toUpperCase());
        draft.setFullName(Validator.requireMaxLength(draft.getFullName(), 80, "Full name"));
        draft.setEmail(Validator.requireEmail(draft.getEmail(), "Email"));
        draft.setMajor(Validator.requireMaxLength(draft.getMajor(), 60, "Major"));
        int maxYear = java.time.Year.now().getValue() + 1;
        Validator.requireRange(draft.getEnrollmentYear(), MIN_YEAR, maxYear, "Enrollment year");

        for (Student s : studentDao.findAll()) {
            if (s.getId() != selfId && s.getCode().equalsIgnoreCase(draft.getCode())) {
                throw new IllegalArgumentException("A student with code " + draft.getCode()
                        + " already exists.");
            }
        }
    }

    private boolean hasActiveEnrollments(int studentId) {
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getStudentId() == studentId && e.getStatus() == EnrollmentStatus.ENROLLED) {
                return true;
            }
        }
        return false;
    }
}
