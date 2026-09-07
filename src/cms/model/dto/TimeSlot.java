package cms.model.dto;

import java.util.Objects;

public final class TimeSlot {

    private final Day day;
    private final int startMinutes;
    private final int endMinutes;

    public TimeSlot(Day day, int startMinutes, int endMinutes) {
        this.day = Objects.requireNonNull(day, "day");
        if (startMinutes < 0 || endMinutes > 24 * 60) {
            throw new IllegalArgumentException("Meeting time must be inside one day.");
        }
        if (startMinutes >= endMinutes) {
            throw new IllegalArgumentException("Meeting must start before it ends.");
        }
        this.startMinutes = startMinutes;
        this.endMinutes = endMinutes;
    }

    public static TimeSlot parse(String token) {
        String t = token == null ? "" : token.trim();
        String[] parts = t.split("\\s+");
        if (parts.length != 2 || !parts[1].contains("-")) {
            throw new IllegalArgumentException(
                    "Bad time slot '" + token + "' (want e.g. MON 09:00-10:30).");
        }
        Day day = Day.fromCode(parts[0]);
        String[] range = parts[1].split("-", 2);
        return new TimeSlot(day, parseHhMm(range[0]), parseHhMm(range[1]));
    }

    private static int parseHhMm(String hhmm) {
        String[] p = hhmm.trim().split(":");
        if (p.length != 2) {
            throw new IllegalArgumentException("Bad time '" + hhmm + "' (want HH:MM).");
        }
        try {
            int h = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) {
                throw new IllegalArgumentException("Time '" + hhmm + "' is out of range.");
            }
            return h * 60 + m;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Bad time '" + hhmm + "' (want HH:MM).");
        }
    }

    public Day getDay() {
        return day;
    }

    public int getStartMinutes() {
        return startMinutes;
    }

    public int getEndMinutes() {
        return endMinutes;
    }

    public boolean overlaps(TimeSlot other) {
        return day == other.day
                && startMinutes < other.endMinutes
                && other.startMinutes < endMinutes;
    }

    public String format() {
        return day.name() + " " + hhmm(startMinutes) + "-" + hhmm(endMinutes);
    }

    private static String hhmm(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    @Override
    public String toString() {
        return format();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeSlot)) {
            return false;
        }
        TimeSlot that = (TimeSlot) o;
        return startMinutes == that.startMinutes
                && endMinutes == that.endMinutes
                && day == that.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startMinutes, endMinutes);
    }
}
