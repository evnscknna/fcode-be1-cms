package cms.model.report;

public final class CourseStatistics {

    private final int courseId;
    private final String code;
    private final String title;
    private final int capacity;
    private final int enrolled;
    private final int waitlistSize;
    private final int withdrawnCount;
    private final int completedCount;

    public CourseStatistics(int courseId, String code, String title, int capacity,
                            int enrolled, int waitlistSize, int withdrawnCount,
                            int completedCount) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.capacity = capacity;
        this.enrolled = enrolled;
        this.waitlistSize = waitlistSize;
        this.withdrawnCount = withdrawnCount;
        this.completedCount = completedCount;
    }

    public int getCourseId() {
        return courseId;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolled() {
        return enrolled;
    }

    public int getSeatsLeft() {
        return Math.max(0, capacity - enrolled);
    }

    public double getFillRatePct() {
        return capacity == 0 ? 0.0 : (enrolled * 100.0) / capacity;
    }

    public int getWaitlistSize() {
        return waitlistSize;
    }

    public int getWithdrawnCount() {
        return withdrawnCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }
}
