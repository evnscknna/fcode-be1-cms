package cms.view;

import java.util.List;

public class EnrollmentView {

    private final ConsoleView view;

    public EnrollmentView(ConsoleView view) {
        this.view = view;
    }

    public void showWaitlist(String courseLabel, List<List<String>> rows) {
        view.subheading("Waiting list - " + courseLabel);
        view.table(java.util.Arrays.asList("Pos", "StudentId", "Code", "Name", "Joined"), rows);
    }

    public void showHistory(List<String> undo, List<String> redo) {
        view.subheading("Undo history (newest first)");
        if (undo.isEmpty()) {
            view.info("(nothing to undo)");
        } else {
            for (String s : undo) {
                view.bullet(s);
            }
        }
        view.subheading("Redo history (newest first)");
        if (redo.isEmpty()) {
            view.info("(nothing to redo)");
        } else {
            for (String s : redo) {
                view.bullet(s);
            }
        }
    }
}
