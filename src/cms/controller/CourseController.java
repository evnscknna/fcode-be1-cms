package cms.controller;

import cms.model.dto.Course;
import cms.model.dto.Student;
import cms.model.service.CourseService;
import cms.model.service.EnrollmentService;
import cms.model.service.InstructorService;
import cms.model.service.WaitlistService;
import cms.view.ConsoleView;
import cms.view.CourseView;
import cms.view.MenuView;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

public class CourseController {

    private final ConsoleView view;
    private final CourseView courseView;
    private final CourseService courseService;
    private final InstructorService instructorService;
    private final EnrollmentService enrollmentService;
    private final WaitlistService waitlistService;

    public CourseController(ConsoleView view, CourseService courseService,
                            InstructorService instructorService, EnrollmentService enrollmentService,
                            WaitlistService waitlistService) {
        this.view = view;
        this.courseView = new CourseView(view);
        this.courseService = courseService;
        this.instructorService = instructorService;
        this.enrollmentService = enrollmentService;
        this.waitlistService = waitlistService;
    }

    public void run() {
        List<String> options = Arrays.asList(
                "List all courses", "View course details", "Add course", "Edit course",
                "Delete course", "Search courses", "Sort courses");
        while (true) {
            int choice = MenuView.choose(view, "Courses", options);
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
                courseView.showList(courseService.listAll());
                break;
            case 2:
                viewDetails();
                break;
            case 3:
                add();
                break;
            case 4:
                edit();
                break;
            case 5:
                delete();
                break;
            case 6:
                courseView.showList(courseService.search(view.readLine("Search text")));
                break;
            case 7:
                sort();
                break;
            default:
                break;
        }
    }

    private void viewDetails() {
        Course course = courseService.get(view.readInt("Course id"));
        String instructor = course.hasInstructor()
                ? instructorName(course.getInstructorId()) : "(unassigned)";
        List<Student> roster = enrollmentService.enrolledStudents(course.getId());
        int waiting = waitlistService.queueFor(course.getId()).size();
        courseView.showDetails(course, instructor, roster, waiting);
    }

    private void add() {
        Course saved = courseService.create(courseView.readNewForm());
        view.success("Created course " + saved.getCode() + " (id " + saved.getId() + ").");
    }

    private void edit() {
        Course current = courseService.get(view.readInt("Course id to edit"));
        int promoted = courseService.update(current.getId(), courseView.readEditForm(current));
        view.success("Course " + current.getCode() + " updated.");
        if (promoted > 0) {
            view.success("Capacity went up - " + promoted
                    + " student(s) auto-enrolled from the waiting list.");
        }
    }

    private void delete() {
        Course course = courseService.get(view.readInt("Course id to delete"));
        if (view.confirm("Delete " + course.getCode() + " - " + course.getTitle() + "?")) {
            courseService.delete(course.getId());
            view.success("Course deleted.");
        }
    }

    private void sort() {
        String key = view.readLine("Sort by (code / title / credits / capacity)");
        courseView.showList(courseService.sortedBy(key.isEmpty() ? "code" : key));
    }

    private String instructorName(int instructorId) {
        try {
            return instructorService.get(instructorId).getFullName();
        } catch (IllegalArgumentException missing) {
            return "#" + instructorId + " (missing)";
        }
    }
}
