package cms.model.service.history;

import cms.model.dao.EnrollmentDao;
import cms.model.dto.Enrollment;

public final class AddEnrollmentCommand implements Command {

    private final EnrollmentDao dao;
    private final Enrollment enrollment;

    public AddEnrollmentCommand(EnrollmentDao dao, Enrollment enrollment) {
        this.dao = dao;
        this.enrollment = enrollment;
    }

    @Override
    public void execute() {
        dao.insert(enrollment); // same object, fresh id (also on redo)
    }

    @Override
    public void undo() {
        dao.deleteById(enrollment.getId());
    }

    @Override
    public String description() {
        return "enroll student #" + enrollment.getStudentId()
                + " in course #" + enrollment.getCourseId();
    }
}
