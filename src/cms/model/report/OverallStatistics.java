package cms.model.report;

public final class OverallStatistics {

    private final int totalCourses;
    private final int totalActiveEnrollments;
    private final int totalWaitlisted;
    private final double averageFillRatePct;
    private final String mostPopularLabel;
    private final int mostPopularCount;
    private final String leastPopularLabel;
    private final int leastPopularCount;

    public OverallStatistics(int totalCourses, int totalActiveEnrollments, int totalWaitlisted,
                             double averageFillRatePct, String mostPopularLabel, int mostPopularCount,
                             String leastPopularLabel, int leastPopularCount) {
        this.totalCourses = totalCourses;
        this.totalActiveEnrollments = totalActiveEnrollments;
        this.totalWaitlisted = totalWaitlisted;
        this.averageFillRatePct = averageFillRatePct;
        this.mostPopularLabel = mostPopularLabel;
        this.mostPopularCount = mostPopularCount;
        this.leastPopularLabel = leastPopularLabel;
        this.leastPopularCount = leastPopularCount;
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public int getTotalActiveEnrollments() {
        return totalActiveEnrollments;
    }

    public int getTotalWaitlisted() {
        return totalWaitlisted;
    }

    public double getAverageFillRatePct() {
        return averageFillRatePct;
    }

    public String getMostPopularLabel() {
        return mostPopularLabel;
    }

    public int getMostPopularCount() {
        return mostPopularCount;
    }

    public String getLeastPopularLabel() {
        return leastPopularLabel;
    }

    public int getLeastPopularCount() {
        return leastPopularCount;
    }
}
