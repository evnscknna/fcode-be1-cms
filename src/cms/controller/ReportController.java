package cms.controller;

import cms.model.dto.Course;
import cms.model.dto.Student;
import cms.model.service.CourseService;
import cms.model.service.ProgressReportService;
import cms.model.service.StatisticsService;
import cms.model.service.StudentService;
import cms.view.ConsoleView;
import cms.view.MenuView;
import cms.view.ReportView;
import cms.view.StudentView;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

public class ReportController {

    private final ConsoleView view;
    private final ReportView reportView;
    private final StudentView studentView;
    private final StatisticsService statisticsService;
    private final ProgressReportService progressReportService;
    private final CourseService courseService;
    private final StudentService studentService;

    public ReportController(ConsoleView view, StatisticsService statisticsService,
                            ProgressReportService progressReportService, CourseService courseService,
                            StudentService studentService) {
        this.view = view;
        this.reportView = new ReportView(view);
        this.studentView = new StudentView(view);
        this.statisticsService = statisticsService;
        this.progressReportService = progressReportService;
        this.courseService = courseService;
        this.studentService = studentService;
    }

    public void run() {
        List<String> options = Arrays.asList(
                "Enrollment statistics - all courses", "Statistics - one course",
                "Overall statistics", "Student progress report");
        while (true) {
            int choice = MenuView.choose(view, "Reports & Statistics", options);
            if (choice == 0) {
                return;
            }
            try {
                dispatch(choice);
            } catch (IllegalArgumentException | IllegalStateException | UncheckedIOException error) {
                view.error(error.getMessage());
            }
        }
    }

    private void dispatch(int choice) {
        switch (choice) {
            case 1:
                reportView.showAllCourseStatistics(statisticsService.forAllCourses());
                break;
            case 2:
                Course course = courseService.get(view.readInt("Course id"));
                reportView.showCourseStatistics(statisticsService.forCourse(course.getId()));
                break;
            case 3:
                reportView.showOverall(statisticsService.overall());
                break;
            case 4:
                Student student = studentService.get(view.readInt("Student id"));
                studentView.showProgressReport(progressReportService.forStudent(student.getId()));
                break;
            default:
                break;
        }
    }
}
