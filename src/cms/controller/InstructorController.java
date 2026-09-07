package cms.controller;

import cms.model.dto.Instructor;
import cms.model.service.InstructorService;
import cms.model.service.ScheduleService;
import cms.view.ConsoleView;
import cms.view.InstructorView;
import cms.view.MenuView;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;

public class InstructorController {

    private final ConsoleView view;
    private final InstructorView instructorView;
    private final InstructorService instructorService;
    private final ScheduleService scheduleService;

    public InstructorController(ConsoleView view, InstructorService instructorService,
                                ScheduleService scheduleService) {
        this.view = view;
        this.instructorView = new InstructorView(view);
        this.instructorService = instructorService;
        this.scheduleService = scheduleService;
    }

    public void run() {
        List<String> options = Arrays.asList(
                "List all instructors", "View teaching schedule", "Add instructor",
                "Edit instructor", "Delete instructor", "Search instructors", "Sort instructors");
        while (true) {
            int choice = MenuView.choose(view, "Instructors", options);
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
                instructorView.showList(instructorService.listAll());
                break;
            case 2:
                showSchedule();
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
                instructorView.showList(instructorService.search(view.readLine("Search text")));
                break;
            case 7:
                sort();
                break;
            default:
                break;
        }
    }

    private void showSchedule() {
        Instructor instructor = instructorService.get(view.readInt("Instructor id"));
        instructorView.showTeachingSchedule(instructor,
                scheduleService.instructorSchedule(instructor.getId()));
    }

    private void add() {
        Instructor saved = instructorService.create(instructorView.readNewForm());
        view.success("Created instructor " + saved.getCode() + " (id " + saved.getId() + ").");
    }

    private void edit() {
        Instructor current = instructorService.get(view.readInt("Instructor id to edit"));
        instructorService.update(current.getId(), instructorView.readEditForm(current));
        view.success("Instructor " + current.getCode() + " updated.");
    }

    private void delete() {
        Instructor instructor = instructorService.get(view.readInt("Instructor id to delete"));
        if (view.confirm("Delete " + instructor.getCode() + " - " + instructor.getFullName() + "?")) {
            instructorService.delete(instructor.getId());
            view.success("Instructor deleted.");
        }
    }

    private void sort() {
        String key = view.readLine("Sort by (name / code / department)");
        instructorView.showList(instructorService.sortedBy(key.isEmpty() ? "name" : key));
    }
}
