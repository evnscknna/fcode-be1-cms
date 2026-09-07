package cms.controller;

import cms.model.dto.Admin;
import cms.model.service.AuthService;
import cms.view.ConsoleView;
import cms.view.MenuView;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class AppController {

    private static final int MAX_LOGIN_ATTEMPTS = 3;

    private final ConsoleView view;
    private final AuthService authService;
    private final CourseController courseController;
    private final StudentController studentController;
    private final InstructorController instructorController;
    private final EnrollmentController enrollmentController;
    private final ReportController reportController;
    private final Runnable saveAll;

    public AppController(ConsoleView view, AuthService authService, CourseController courseController,
                        StudentController studentController, InstructorController instructorController,
                        EnrollmentController enrollmentController, ReportController reportController,
                        Runnable saveAll) {
        this.view = view;
        this.authService = authService;
        this.courseController = courseController;
        this.studentController = studentController;
        this.instructorController = instructorController;
        this.enrollmentController = enrollmentController;
        this.reportController = reportController;
        this.saveAll = saveAll;
    }

    public void run() {
        view.heading("Course Management System");
        Admin admin = login();
        if (admin == null) {
            view.println("Goodbye.");
            return;
        }
        view.success("Signed in as " + admin.getUsername() + ".");
        mainMenu();
    }

    private Admin login() {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            try {
                String username = view.readLine("Username");
                String password = view.readLine("Password");
                return authService.login(username, password);
            } catch (NoSuchElementException endOfInput) {
                return null;
            } catch (IllegalArgumentException wrong) {
                view.error(wrong.getMessage() + "  (attempt " + attempt + "/" + MAX_LOGIN_ATTEMPTS + ")");
            }
        }
        view.error("Too many failed attempts.");
        return null;
    }

    private void mainMenu() {
        List<String> options = Arrays.asList(
                "Courses", "Students", "Instructors", "Enrollments",
                "Reports & statistics", "Save & exit");
        while (true) {
            try {
                int choice = MenuView.choose(view, "Main Menu", options);
                if (choice == 0 || choice == 6) {
                    break;
                }
                dispatch(choice);
            } catch (NoSuchElementException endOfInput) { // Ctrl+Z / piped EOF -> quit
                view.blankLine();
                break;
            }
        }
        saveAll.run();
        view.success("All data saved. Goodbye.");
    }

    private void dispatch(int choice) {
        switch (choice) {
            case 1:
                courseController.run();
                break;
            case 2:
                studentController.run();
                break;
            case 3:
                instructorController.run();
                break;
            case 4:
                enrollmentController.run();
                break;
            case 5:
                reportController.run();
                break;
            default:
                break;
        }
    }
}
