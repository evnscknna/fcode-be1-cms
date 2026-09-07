package cms.controller;

import cms.model.dto.Course;
import cms.model.dto.Student;
import cms.model.dto.WaitlistEntry;
import cms.model.service.CourseService;
import cms.model.service.EnrollmentService;
import cms.model.service.StudentService;
import cms.model.service.WaitlistService;
import cms.util.TimeFormat;
import cms.view.ConsoleView;
import cms.view.EnrollmentView;
import cms.view.MenuView;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnrollmentController {

    private final ConsoleView view;
    private final EnrollmentView enrollmentView;
    private final EnrollmentService enrollmentService;
    private final WaitlistService waitlistService;
    private final CourseService courseService;
    private final StudentService studentService;

    public EnrollmentController(ConsoleView view, EnrollmentService enrollmentService,
                                WaitlistService waitlistService, CourseService courseService,
                                StudentService studentService) {
        this.view = view;
        this.enrollmentView = new EnrollmentView(view);
        this.enrollmentService = enrollmentService;
        this.waitlistService = waitlistService;
        this.courseService = courseService;
        this.studentService = studentService;
    }

    public void run() {
        List<String> options = Arrays.asList(
                "Enroll student", "Withdraw student", "View waiting list",
                "Remove student from waiting list", "Record course completion / grade",
                "Undo last operation", "Redo", "Show operation history");
        while (true) {
            int choice = MenuView.choose(view, "Enrollments", options);
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
                enroll();
                break;
            case 2:
                withdraw();
                break;
            case 3:
                viewWaitlist();
                break;
            case 4:
                removeFromWaitlist();
                break;
            case 5:
                recordCompletion();
                break;
            case 6:
                view.success("Undone: " + enrollmentService.undo());
                break;
            case 7:
                view.success("Redone: " + enrollmentService.redo());
                break;
            case 8:
                enrollmentView.showHistory(enrollmentService.undoHistory(),
                        enrollmentService.redoHistory());
                break;
            default:
                break;
        }
    }

    private void enroll() {
        int studentId = view.readInt("Student id");
        int courseId = view.readInt("Course id");
        if (enrollmentService.enroll(studentId, courseId)) {
            view.success("Student enrolled.");
            return;
        }
        view.info(courseService.get(courseId).getCode() + " is full.");
        if (view.confirm("Add the student to the waiting list?")) {
            enrollmentService.joinWaitlist(studentId, courseId);
            view.success("Added to the waiting list at position "
                    + waitlistService.positionOf(studentId, courseId) + ".");
        }
    }

    private void withdraw() {
        int studentId = view.readInt("Student id");
        int courseId = view.readInt("Course id");
        int promoted = enrollmentService.withdraw(studentId, courseId);
        view.success("Student withdrawn."
                + (promoted > 0 ? " Pulled " + promoted + " student(s) off the waiting list." : ""));
    }

    private void viewWaitlist() {
        Course course = courseService.get(view.readInt("Course id"));
        List<WaitlistEntry> queue = waitlistService.queueFor(course.getId());
        List<List<String>> rows = new ArrayList<>();
        int position = 1;
        for (WaitlistEntry entry : queue) {
            Student student = safeStudent(entry.getStudentId());
            rows.add(Arrays.asList(
                    String.valueOf(position++),
                    String.valueOf(entry.getStudentId()),
                    student == null ? "-" : student.getCode(),
                    student == null ? "(missing)" : student.getFullName(),
                    TimeFormat.human(entry.getTimestamp())));
        }
        enrollmentView.showWaitlist(course.getCode() + " - " + course.getTitle(), rows);
    }

    private void removeFromWaitlist() {
        int studentId = view.readInt("Student id");
        int courseId = view.readInt("Course id");
        waitlistService.removeFromWaitlist(studentId, courseId);
        view.success("Removed from the waiting list.");
    }

    private void recordCompletion() {
        int studentId = view.readInt("Student id");
        int courseId = view.readInt("Course id");
        String grade = view.readRequired("Grade (A, A-, B+, B, ... F)");
        enrollmentService.recordCompletion(studentId, courseId, grade);
        view.success("Enrollment marked COMPLETED with grade " + grade.toUpperCase()
                + "; waiting list processed.");
    }

    private Student safeStudent(int studentId) {
        try {
            return studentService.get(studentId);
        } catch (IllegalArgumentException missing) {
            return null;
        }
    }
}
