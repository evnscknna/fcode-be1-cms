package cms.view;

import cms.model.dto.Student;
import cms.model.report.ProgressReport;
import cms.model.report.ScheduleRow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudentView {

    private final ConsoleView view;

    public StudentView(ConsoleView view) {
        this.view = view;
    }

    public void showList(List<Student> students) {
        List<List<String>> rows = new ArrayList<>();
        for (Student s : students) {
            rows.add(Arrays.asList(String.valueOf(s.getId()), s.getCode(), s.getFullName(),
                    s.getEmail(), s.getMajor(), String.valueOf(s.getEnrollmentYear())));
        }
        view.table(Arrays.asList("ID", "Code", "Name", "Email", "Major", "Year"), rows);
    }

    public Student readNewForm() {
        Student draft = new Student();
        draft.setCode(view.readRequired("Student code (e.g. SE1701)"));
        draft.setFullName(view.readRequired("Full name"));
        draft.setEmail(view.readRequired("Email"));
        draft.setMajor(view.readRequired("Major"));
        draft.setEnrollmentYear(view.readInt("Enrollment year"));
        return draft;
    }

    public Student readEditForm(Student current) {
        Student draft = new Student();
        draft.setCode(view.readOrKeep("Student code", current.getCode()));
        draft.setFullName(view.readOrKeep("Full name", current.getFullName()));
        draft.setEmail(view.readOrKeep("Email", current.getEmail()));
        draft.setMajor(view.readOrKeep("Major", current.getMajor()));
        draft.setEnrollmentYear(view.readIntOrKeep("Enrollment year", current.getEnrollmentYear()));
        return draft;
    }

    public void showSchedule(Student student, List<ScheduleRow> rows) {
        view.subheading("Weekly schedule - " + student.getCode() + " " + student.getFullName());
        if (rows.isEmpty()) {
            view.info("No enrolled courses.");
            return;
        }
        List<List<String>> table = new ArrayList<>();
        for (ScheduleRow r : rows) {
            table.add(Arrays.asList(r.getDay().fullName(), r.getSlotText(),
                    r.getCourseCode(), r.getCourseTitle(), r.getNote()));
        }
        view.table(Arrays.asList("Day", "Time", "Course", "Title", "Semester"), table);
    }

    public void showProgressReport(ProgressReport report) {
        view.subheading("Progress report - " + report.getStudentName());
        view.info(String.format("Completed credits : %d / %d (%.1f%%)",
                report.getCompletedCredits(), report.getTargetCredits(), report.getCompletionPct()));
        view.info("In-progress credits: " + report.getInProgressCredits());
        view.info("Remaining credits  : " + report.getRemainingCredits());
        view.info("GPA                : " + (report.hasGpa()
                ? String.format("%.2f", report.getGpa()) : "n/a (no graded courses)"));

        for (ProgressReport.SemesterBlock block : report.getSemesters()) {
            view.subheading("Semester " + block.getSemester());
            List<List<String>> rows = new ArrayList<>();
            for (ProgressReport.Line line : block.getLines()) {
                rows.add(Arrays.asList(line.getCourseCode(), line.getCourseTitle(),
                        String.valueOf(line.getCredits()), line.getStatus(),
                        line.getGrade() == null || line.getGrade().isEmpty() ? "-" : line.getGrade()));
            }
            view.table(Arrays.asList("Course", "Title", "Cr", "Status", "Grade"), rows);
        }
    }
}
