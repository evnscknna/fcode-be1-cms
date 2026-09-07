package cms.model.dto;

public enum Day {

    MON("Monday"),
    TUE("Tuesday"),
    WED("Wednesday"),
    THU("Thursday"),
    FRI("Friday"),
    SAT("Saturday"),
    SUN("Sunday");

    private final String fullName;

    Day(String fullName) {
        this.fullName = fullName;
    }

    public String fullName() {
        return fullName;
    }

    public static Day fromCode(String code) {
        if (code != null) {
            String c = code.trim().toUpperCase();
            for (Day d : values()) {
                if (d.name().equals(c)) {
                    return d;
                }
            }
        }
        throw new IllegalArgumentException(
                "Unknown day '" + code + "' (use MON, TUE, WED, THU, FRI, SAT, SUN).");
    }
}
