package cms.model.service.history;

public interface Command {

    void execute();

    void undo();

    String description();
}
