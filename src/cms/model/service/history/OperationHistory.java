package cms.model.service.history;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// Undo/redo stacks for this run only. A new action clears the redo stack.
public final class OperationHistory {

    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void perform(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    // Called when the data changed in a way the stack cannot walk back.
    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public String undo() {
        if (!canUndo()) {
            throw new IllegalStateException("Nothing to undo.");
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return command.description();
    }

    public String redo() {
        if (!canRedo()) {
            throw new IllegalStateException("Nothing to redo.");
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return command.description();
    }

    public List<String> undoHistory() {
        List<String> out = new ArrayList<>();
        for (Command c : undoStack) {
            out.add(c.description());
        }
        return out;
    }

    public List<String> redoHistory() {
        List<String> out = new ArrayList<>();
        for (Command c : redoStack) {
            out.add(c.description());
        }
        return out;
    }
}
