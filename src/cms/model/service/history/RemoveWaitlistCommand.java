package cms.model.service.history;

import cms.model.dao.WaitlistDao;
import cms.model.dto.WaitlistEntry;

public final class RemoveWaitlistCommand implements Command {

    private final WaitlistDao dao;
    private final WaitlistEntry entry;

    public RemoveWaitlistCommand(WaitlistDao dao, WaitlistEntry entry) {
        this.dao = dao;
        this.entry = entry;
    }

    @Override
    public void execute() {
        dao.deleteById(entry.getId());
    }

    @Override
    public void undo() {
        dao.insert(entry); // same data back, new id
    }

    @Override
    public String description() {
        return "remove student #" + entry.getStudentId()
                + " from waiting list of course #" + entry.getCourseId();
    }
}
