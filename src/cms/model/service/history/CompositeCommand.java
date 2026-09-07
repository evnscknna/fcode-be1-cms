package cms.model.service.history;

import java.util.ArrayList;
import java.util.List;

public final class CompositeCommand implements Command {

    private final String description;
    private final List<Command> steps;

    public CompositeCommand(String description, List<Command> steps) {
        this.description = description;
        this.steps = new ArrayList<>(steps);
    }

    @Override
    public void execute() {
        for (Command step : steps) {
            step.execute();
        }
    }

    @Override
    public void undo() {
        for (int i = steps.size() - 1; i >= 0; i--) {
            steps.get(i).undo();
        }
    }

    @Override
    public String description() {
        return description;
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }
}
