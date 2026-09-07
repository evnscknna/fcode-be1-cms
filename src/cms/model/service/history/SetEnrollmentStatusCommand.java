package cms.model.service.history;

import cms.model.dao.EnrollmentDao;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;

public final class SetEnrollmentStatusCommand implements Command {

    private final EnrollmentDao dao;
    private final int enrollmentId;
    private final EnrollmentStatus from;
    private final EnrollmentStatus to;
    private long previousTimestamp;

    public SetEnrollmentStatusCommand(EnrollmentDao dao, int enrollmentId,
                                      EnrollmentStatus from, EnrollmentStatus to) {
        this.dao = dao;
        this.enrollmentId = enrollmentId;
        this.from = from;
        this.to = to;
    }

    @Override
    public void execute() {
        Enrollment e = dao.getById(enrollmentId);
        previousTimestamp = e.getTimestamp();
        e.setStatus(to);
        e.setTimestamp(System.currentTimeMillis());
        dao.update(e);
    }

    @Override
    public void undo() {
        Enrollment e = dao.getById(enrollmentId);
        e.setStatus(from);
        e.setTimestamp(previousTimestamp);
        dao.update(e);
    }

    @Override
    public String description() {
        return "change enrollment #" + enrollmentId + " from " + from + " to " + to;
    }
}
