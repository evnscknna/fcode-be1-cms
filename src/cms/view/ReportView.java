package cms.view;

import cms.model.report.CourseStatistics;
import cms.model.report.OverallStatistics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportView {

    private final ConsoleView view;

    public ReportView(ConsoleView view) {
        this.view = view;
    }

    public void showAllCourseStatistics(List<CourseStatistics> stats) {
        view.subheading("Course enrollment statistics");
        List<List<String>> rows = new ArrayList<>();
        for (CourseStatistics s : stats) {
            rows.add(Arrays.asList(
                    s.getCode(),
                    s.getTitle(),
                    s.getEnrolled() + "/" + s.getCapacity(),
                    String.valueOf(s.getSeatsLeft()),
                    String.format("%.0f%%", s.getFillRatePct()),
                    String.valueOf(s.getWaitlistSize()),
                    String.valueOf(s.getWithdrawnCount()),
                    String.valueOf(s.getCompletedCount())));
        }
        view.table(Arrays.asList("Code", "Title", "Enr/Cap", "Free", "Fill", "Wait", "Wdn", "Cmp"), rows);
    }

    public void showCourseStatistics(CourseStatistics s) {
        view.subheading("Statistics - " + s.getCode() + " " + s.getTitle());
        view.info("Enrolled     : " + s.getEnrolled() + " / " + s.getCapacity());
        view.info("Seats left   : " + s.getSeatsLeft());
        view.info(String.format("Fill rate    : %.1f%%", s.getFillRatePct()));
        view.info("Waiting list : " + s.getWaitlistSize());
        view.info("Withdrawn    : " + s.getWithdrawnCount());
        view.info("Completed    : " + s.getCompletedCount());
    }

    public void showOverall(OverallStatistics o) {
        view.subheading("Overall statistics");
        view.info("Courses               : " + o.getTotalCourses());
        view.info("Active enrollments    : " + o.getTotalActiveEnrollments());
        view.info("Students waiting       : " + o.getTotalWaitlisted());
        view.info(String.format("Average fill rate     : %.1f%%", o.getAverageFillRatePct()));
        view.info("Most popular course   : " + o.getMostPopularLabel()
                + " (" + o.getMostPopularCount() + " enrolled)");
        view.info("Least popular course  : " + o.getLeastPopularLabel()
                + " (" + o.getLeastPopularCount() + " enrolled)");
    }
}
