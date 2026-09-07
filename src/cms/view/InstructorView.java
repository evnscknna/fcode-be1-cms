package cms.view;

import cms.model.dto.Instructor;
import cms.model.report.ScheduleRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstructorView {

    private final ConsoleView view;

    public InstructorView(ConsoleView view) {
        this.view = view;
    }

    public void showList(List<Instructor> instructors) {
        List<List<String>> rows = new ArrayList<>();
        for (Instructor i : instructors) {
            rows.add(Arrays.asList(String.valueOf(i.getId()), i.getCode(), i.getFullName(),
                    i.getEmail(), i.getDepartment()));
        }
        view.table(Arrays.asList("ID", "Code", "Name", "Email", "Department"), rows);
    }

    public Instructor readNewForm() {
        Instructor draft = new Instructor();
        draft.setCode(view.readRequired("Instructor code (e.g. INS01)"));
        draft.setFullName(view.readRequired("Full name"));
        draft.setEmail(view.readRequired("Email"));
        draft.setDepartment(view.readRequired("Department"));
        return draft;
    }

    public Instructor readEditForm(Instructor current) {
        Instructor draft = new Instructor();
        draft.setCode(view.readOrKeep("Instructor code", current.getCode()));
        draft.setFullName(view.readOrKeep("Full name", current.getFullName()));
        draft.setEmail(view.readOrKeep("Email", current.getEmail()));
        draft.setDepartment(view.readOrKeep("Department", current.getDepartment()));
        return draft;
    }

    public void showTeachingSchedule(Instructor instructor, List<ScheduleRow> rows) {
        view.subheading("Teaching schedule - " + instructor.getCode() + " " + instructor.getFullName());
        if (rows.isEmpty()) {
            view.info("No courses assigned.");
            return;
        }
        List<List<String>> table = new ArrayList<>();
        for (ScheduleRow r : rows) {
            table.add(Arrays.asList(r.getDay().fullName(), r.getSlotText(),
                    r.getCourseCode(), r.getCourseTitle(), r.getNote()));
        }
        view.table(Arrays.asList("Day", "Time", "Course", "Title", "Semester"), table);
    }
}
