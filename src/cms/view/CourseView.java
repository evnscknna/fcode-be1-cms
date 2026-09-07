package cms.view;

import cms.model.dto.Course;
import cms.model.dto.Student;
import cms.model.dto.TimeSlot;
import java.util.ArrayList;
import java.util.List;

public class CourseView {

    private final ConsoleView view;

    public CourseView(ConsoleView view) {
        this.view = view;
    }

    public void showList(List<Course> courses) {
        List<String> headers = java.util.Arrays.asList(
                "ID", "Code", "Title", "Cr", "Cap", "Semester", "InstrId", "Prereqs");
        List<List<String>> rows = new ArrayList<>();
        for (Course c : courses) {
            rows.add(java.util.Arrays.asList(
                    String.valueOf(c.getId()),
                    c.getCode(),
                    c.getTitle(),
                    String.valueOf(c.getCredits()),
                    String.valueOf(c.getCapacity()),
                    c.getSemester(),
                    c.hasInstructor() ? String.valueOf(c.getInstructorId()) : "-",
                    c.getPrerequisites().isEmpty() ? "-" : String.join(";", c.getPrerequisites())));
        }
        view.table(headers, rows);
    }

    public void showDetails(Course c, String instructorName, List<Student> roster, int waitlistCount) {
        view.subheading("Course " + c.getCode() + " - " + c.getTitle());
        view.info("Id           : " + c.getId());
        view.info("Credits      : " + c.getCredits());
        view.info("Capacity     : " + c.getCapacity() + " (enrolled " + roster.size() + ")");
        view.info("Semester     : " + c.getSemester());
        view.info("Instructor   : " + instructorName);
        view.info("Schedule     : " + (c.getSchedule().isEmpty() ? "(none)" : formatSchedule(c)));
        view.info("Prerequisites: " + (c.getPrerequisites().isEmpty()
                ? "(none)" : String.join(", ", c.getPrerequisites())));
        view.info("Waiting list : " + waitlistCount);

        view.subheading("Enrolled students (" + roster.size() + ")");
        if (roster.isEmpty()) {
            view.info("(none)");
            return;
        }
        List<List<String>> rows = new ArrayList<>();
        for (Student s : roster) {
            rows.add(java.util.Arrays.asList(String.valueOf(s.getId()), s.getCode(),
                    s.getFullName(), s.getMajor()));
        }
        view.table(java.util.Arrays.asList("ID", "Code", "Name", "Major"), rows);
    }

    public Course readNewForm() {
        Course draft = new Course();
        draft.setCode(view.readRequired("Course code (e.g. CS101)"));
        draft.setTitle(view.readRequired("Title"));
        draft.setCredits(view.readIntInRange("Credits", 1, 12));
        draft.setCapacity(view.readIntInRange("Capacity", 1, 500));
        draft.setSemester(view.readRequired("Semester (e.g. 2026-FALL)"));
        draft.setInstructorId(view.readInt("Instructor id (0 for none)"));
        draft.setSchedule(readSchedule(new ArrayList<>()));
        draft.setPrerequisites(readPrerequisites(new ArrayList<>()));
        return draft;
    }

    public Course readEditForm(Course current) {
        Course draft = new Course();
        draft.setCode(view.readOrKeep("Course code", current.getCode()));
        draft.setTitle(view.readOrKeep("Title", current.getTitle()));
        draft.setCredits(view.readIntOrKeep("Credits", current.getCredits()));
        draft.setCapacity(view.readIntOrKeep("Capacity", current.getCapacity()));
        draft.setSemester(view.readOrKeep("Semester", current.getSemester()));
        draft.setInstructorId(view.readIntOrKeep("Instructor id (0 for none)", current.getInstructorId()));
        draft.setSchedule(readSchedule(current.getSchedule()));
        draft.setPrerequisites(readPrerequisites(current.getPrerequisites()));
        return draft;
    }

    private List<TimeSlot> readSchedule(List<TimeSlot> current) {
        String hint = current.isEmpty() ? "(none)" : joinSlots(current);
        while (true) {
            String raw = view.readLine("Schedule, ';'-separated e.g. MON 09:00-10:30;WED 14:00-15:30 ["
                    + hint + "]");
            if (raw.isEmpty()) {
                return new ArrayList<>(current);
            }
            if (raw.equalsIgnoreCase("none")) {
                return new ArrayList<>();
            }
            try {
                List<TimeSlot> slots = new ArrayList<>();
                for (String token : raw.split(";")) {
                    if (!token.trim().isEmpty()) {
                        slots.add(TimeSlot.parse(token.trim()));
                    }
                }
                return slots;
            } catch (RuntimeException ex) {
                view.error(ex.getMessage());
            }
        }
    }

    private List<String> readPrerequisites(List<String> current) {
        String hint = current.isEmpty() ? "(none)" : String.join(";", current);
        String raw = view.readLine("Prerequisite course codes, ';'-separated [" + hint + "]");
        if (raw.isEmpty()) {
            return new ArrayList<>(current);
        }
        if (raw.equalsIgnoreCase("none")) {
            return new ArrayList<>();
        }
        List<String> codes = new ArrayList<>();
        for (String token : raw.split(";")) {
            if (!token.trim().isEmpty()) {
                codes.add(token.trim());
            }
        }
        return codes;
    }

    private static String formatSchedule(Course c) {
        return joinSlots(c.getSchedule());
    }

    private static String joinSlots(List<TimeSlot> slots) {
        List<String> parts = new ArrayList<>();
        for (TimeSlot s : slots) {
            parts.add(s.format());
        }
        return String.join(", ", parts);
    }
}
