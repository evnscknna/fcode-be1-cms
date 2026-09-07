package cms.view;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

// The only class that touches System.in / System.out.
public class ConsoleView {

    private final Scanner scanner;
    private final PrintStream out;

    public ConsoleView() {
        this(new Scanner(System.in), System.out);
    }

    public ConsoleView(Scanner scanner, PrintStream out) {
        this.scanner = scanner;
        this.out = out;
    }

    public void println(String text) {
        out.println(text);
    }

    public void blankLine() {
        out.println();
    }

    public void heading(String title) {
        String bar = repeat('=', title.length() + 8);
        out.println();
        out.println(bar);
        out.println("=== " + title + " ===");
        out.println(bar);
    }

    public void subheading(String title) {
        out.println();
        out.println("-- " + title + " --");
    }

    public void info(String text) {
        out.println("    " + text);
    }

    public void bullet(String text) {
        out.println("  - " + text);
    }

    public void success(String text) {
        out.println("[OK] " + text);
    }

    public void error(String text) {
        out.println("[!] " + text);
    }

    public String readLine(String prompt) {
        out.print(prompt + ": ");
        out.flush();
        if (!scanner.hasNextLine()) {
            throw new NoSuchElementException("end of input"); // menu loops treat this as quit
        }
        return scanner.nextLine().trim();
    }

    public String readRequired(String label) {
        while (true) {
            String value = readLine(label);
            if (!value.isEmpty()) {
                return value;
            }
            error(label + " is required.");
        }
    }

    public String readOrKeep(String label, String current) {
        String value = readLine(label + " [" + current + "]");
        return value.isEmpty() ? current : value;
    }

    public int readInt(String label) {
        while (true) {
            String raw = readLine(label);
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException ex) {
                error("Please type a whole number.");
            }
        }
    }

    public int readIntInRange(String label, int min, int max) {
        while (true) {
            int value = readInt(label + " (" + min + "-" + max + ")");
            if (value >= min && value <= max) {
                return value;
            }
            error("Must be between " + min + " and " + max + ".");
        }
    }

    public int readIntOrKeep(String label, int current) {
        while (true) {
            String raw = readLine(label + " [" + current + "]");
            if (raw.isEmpty()) {
                return current;
            }
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException ex) {
                error("Please type a whole number.");
            }
        }
    }

    public boolean confirm(String question) {
        while (true) {
            String answer = readLine(question + " (y/n)").toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            error("Please answer y or n.");
        }
    }

    public void table(List<String> headers, List<List<String>> rows) {
        int columns = headers.size();
        int[] widths = new int[columns];
        for (int c = 0; c < columns; c++) {
            widths[c] = headers.get(c).length();
        }
        for (List<String> row : rows) {
            for (int c = 0; c < columns && c < row.size(); c++) {
                widths[c] = Math.max(widths[c], safe(row.get(c)).length());
            }
        }
        out.println(renderRow(headers, widths));
        List<String> separators = new ArrayList<>();
        for (int w : widths) {
            separators.add(repeat('-', w));
        }
        out.println(renderRow(separators, widths));
        if (rows.isEmpty()) {
            out.println("(none)");
            return;
        }
        for (List<String> row : rows) {
            out.println(renderRow(row, widths));
        }
    }

    private String renderRow(List<String> cells, int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < widths.length; c++) {
            String cell = c < cells.size() ? safe(cells.get(c)) : "";
            sb.append(padRight(cell, widths[c]));
            if (c < widths.length - 1) {
                sb.append("  ");
            }
        }
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String padRight(String s, int width) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static String repeat(char ch, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
