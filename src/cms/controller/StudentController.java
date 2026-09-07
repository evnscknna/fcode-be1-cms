package cms.controller;

import cms.model.dto.Student;
import cms.model.service.ProgressReportService;
import cms.model.service.ScheduleService;
import cms.model.service.StudentService;
import cms.view.ConsoleView;
import cms.view.MenuView;
import cms.view.StudentView;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

public class StudentController {

    private final ConsoleView view;
    private final StudentView studentView;
    private final StudentService studentService;
    private final ScheduleService scheduleService;
    private final ProgressReportService progressReportService;

    public StudentController(ConsoleView view, StudentService studentService,
                             ScheduleService scheduleService,
                             ProgressReportService progressReportService) {
        this.view = view;
        this.studentView = new StudentView(view);
        this.studentService = studentService;
        this.scheduleService = scheduleService;
        this.progressReportService = progressReportService;
    }

    public void run() {
        List<String> options = Arrays.asList(
                "List all students", "View schedule", "Progress report", "Add student",
                "Edit student", "Delete student", "Search students", "Sort students");
        while (true) {
            int choice = MenuView.choose(view, "Students", options);
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
                studentView.showList(studentService.listAll());
                break;
            case 2:
                showSchedule();
                break;
            case 3:
                showProgress();
                break;
            case 4:
                add();
                break;
            case 5:
                edit();
                break;
            case 6:
                delete();
                break;
            case 7:
                studentView.showList(studentService.search(view.readLine("Search text")));
                break;
            case 8:
                sort();
                break;
            default:
                break;
        }
    }

    private void showSchedule() {
        Student student = studentService.get(view.readInt("Student id"));
        studentView.showSchedule(student, scheduleService.studentSchedule(student.getId()));
    }

    private void showProgress() {
        Student student = studentService.get(view.readInt("Student id"));
        studentView.showProgressReport(progressReportService.forStudent(student.getId()));
    }

    private void add() {
        Student saved = studentService.create(studentView.readNewForm());
        view.success("Created student " + saved.getCode() + " (id " + saved.getId() + ").");
    }

    private void edit() {
        Student current = studentService.get(view.readInt("Student id to edit"));
        studentService.update(current.getId(), studentView.readEditForm(current));
        view.success("Student " + current.getCode() + " updated.");
    }

    private void delete() {
        Student student = studentService.get(view.readInt("Student id to delete"));
        if (view.confirm("Delete " + student.getCode() + " - " + student.getFullName() + "?")) {
            studentService.delete(student.getId());
            view.success("Student deleted.");
        }
    }

    private void sort() {
        String key = view.readLine("Sort by (name / code / year)");
        studentView.showList(studentService.sortedBy(key.isEmpty() ? "name" : key));
    }
}
