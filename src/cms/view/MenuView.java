package cms.view;

import java.util.List;

public final class MenuView {

    private MenuView() {
    }

    // Returns 0 for Back, or 1..n for an option.
    public static int choose(ConsoleView view, String title, List<String> options) {
        view.heading(title);
        for (int i = 0; i < options.size(); i++) {
            view.println("  " + (i + 1) + ". " + options.get(i));
        }
        view.println("  0. Back");
        return view.readIntInRange("Choose", 0, options.size());
    }
}
