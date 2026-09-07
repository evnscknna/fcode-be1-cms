package cms.util;

import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private Validator() {
    }

    public static String requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty.");
        }
        return value.trim();
    }

    public static String requireMaxLength(String value, int max, String field) {
        String v = requireText(value, field);
        if (v.length() > max) {
            throw new IllegalArgumentException(field + " must be at most " + max + " characters.");
        }
        return v;
    }

    public static int requireRange(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    public static String requireEmail(String value, String field) {
        String v = requireText(value, field);
        if (!EMAIL.matcher(v).matches()) {
            throw new IllegalArgumentException(field + " is not a valid email address.");
        }
        return v;
    }
}
