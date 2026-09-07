package cms.model.dto;

public enum EnrollmentStatus {

    ENROLLED,
    WITHDRAWN,
    COMPLETED;

    public static EnrollmentStatus fromString(String raw) {
        if (raw != null) {
            for (EnrollmentStatus s : values()) {
                if (s.name().equalsIgnoreCase(raw.trim())) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("Unknown enrollment status: " + raw);
    }
}
