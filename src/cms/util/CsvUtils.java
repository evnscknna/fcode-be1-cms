package cms.util;

import java.util.ArrayList;
import java.util.List;

public final class CsvUtils {

    private CsvUtils() {
    }

    public static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"'); // "" inside quotes -> one quote
                        i += 2;
                    } else {
                        inQuotes = false;
                        i++;
                    }
                } else {
                    current.append(c);
                    i++;
                }
            } else if (c == '"') {
                inQuotes = true;
                i++;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
                i++;
            } else {
                current.append(c);
                i++;
            }
        }
        fields.add(current.toString());
        return fields;
    }

    public static String encodeField(String value) {
        String v = value == null ? "" : value;
        boolean mustQuote = v.contains(",") || v.contains("\"")
                || v.contains("\n") || v.contains("\r");
        if (mustQuote) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    public static String toLine(List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(encodeField(fields.get(i)));
        }
        return sb.toString();
    }

    public static String toLine(String... fields) {
        List<String> list = new ArrayList<>(fields.length);
        for (String f : fields) {
            list.add(f);
        }
        return toLine(list);
    }

    public static List<String> splitSub(String encoded) {
        List<String> out = new ArrayList<>();
        if (encoded == null || encoded.trim().isEmpty()) {
            return out;
        }
        for (String token : encoded.split(";")) {
            String t = token.trim();
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    public static String joinSub(List<String> tokens) {
        return String.join(";", tokens);
    }
}
