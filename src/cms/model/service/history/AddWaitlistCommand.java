package cms.model.service.history;

import cms.model.dao.WaitlistDao;
import cms.model.dto.WaitlistEntry;

public final class AddWaitlistCommand implements Command {

    private final WaitlistDao dao;
    private final WaitlistEntry entry;

    public AddWaitlistCommand(WaitlistDao dao, WaitlistEntry entry) {
        this.dao = dao;
        this.entry = entry;
    }

    @Override
    public void execute() {
        dao.insert(entry);
    }

    @Override
    public void undo() {
        dao.deleteById(entry.getId());
    }

    @Override
    public String description() {
        return "add student #" + entry.getStudentId()
                + " to waiting list of course #" + entry.getCourseId();
    }
}
