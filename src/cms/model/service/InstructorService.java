package cms.model.service;

import cms.model.dao.CourseDao;
import cms.model.dao.InstructorDao;
import cms.model.dto.Course;
import cms.model.dto.Instructor;
import cms.util.SearchUtils;
import cms.util.SortUtils;
import cms.util.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InstructorService {

    private final InstructorDao instructorDao;
    private final CourseDao courseDao;

    public InstructorService(InstructorDao instructorDao, CourseDao courseDao) {
        this.instructorDao = instructorDao;
        this.courseDao = courseDao;
    }

    public List<Instructor> listAll() {
        return instructorDao.findAll();
    }

    public Instructor get(int id) {
        return instructorDao.getById(id);
    }

    public Instructor create(Instructor draft) {
        normalizeAndValidate(draft, 0);
        Instructor i = new Instructor(0, draft.getCode(), draft.getFullName(),
                draft.getEmail(), draft.getDepartment());
        return instructorDao.insert(i);
    }

    public void update(int id, Instructor draft) {
        Instructor existing = instructorDao.getById(id);
        normalizeAndValidate(draft, id);
        existing.setCode(draft.getCode());
        existing.setFullName(draft.getFullName());
        existing.setEmail(draft.getEmail());
        existing.setDepartment(draft.getDepartment());
        instructorDao.update(existing);
    }

    public void delete(int id) {
        Instructor instructor = instructorDao.getById(id);
        List<String> assigned = new ArrayList<>();
        for (Course c : courseDao.findAll()) {
            if (c.getInstructorId() == id) {
                assigned.add(c.getCode());
            }
        }
        if (!assigned.isEmpty()) {
            throw new IllegalStateException("Cannot delete " + instructor.getCode()
                    + ": still teaching " + String.join(", ", assigned)
                    + ". Reassign those courses first.");
        }
        instructorDao.deleteById(id);
    }

    public List<Instructor> search(String query) {
        return SearchUtils.filterByText(instructorDao.findAll(), query,
                i -> i.getCode() + " " + i.getFullName() + " " + i.getEmail() + " " + i.getDepartment());
    }

    public List<Instructor> sortedBy(String key) {
        Comparator<Instructor> cmp;
        switch (key == null ? "" : key.trim().toLowerCase()) {
            case "code":
                cmp = Comparator.comparing(Instructor::getCode, String.CASE_INSENSITIVE_ORDER);
                break;
            case "department":
                cmp = Comparator.comparing(Instructor::getDepartment, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Instructor::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "name":
            case "":
                cmp = Comparator.comparing(Instructor::getFullName, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                throw new IllegalArgumentException("Unknown sort key: " + key);
        }
        return SortUtils.sorted(instructorDao.findAll(), cmp);
    }

    private void normalizeAndValidate(Instructor draft, int selfId) {
        draft.setCode(Validator.requireMaxLength(draft.getCode(), 12, "Instructor code").toUpperCase());
        draft.setFullName(Validator.requireMaxLength(draft.getFullName(), 80, "Full name"));
        draft.setEmail(Validator.requireEmail(draft.getEmail(), "Email"));
        draft.setDepartment(Validator.requireMaxLength(draft.getDepartment(), 60, "Department"));

        for (Instructor i : instructorDao.findAll()) {
            if (i.getId() != selfId && i.getCode().equalsIgnoreCase(draft.getCode())) {
                throw new IllegalArgumentException("An instructor with code " + draft.getCode()
                        + " already exists.");
            }
        }
    }
}
