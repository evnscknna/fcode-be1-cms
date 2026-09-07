package cms.model.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Course {

    private int id;
    private String code;
    private String title;
    private int credits;
    private int capacity;
    private String semester;
    private int instructorId;
    private final List<TimeSlot> schedule = new ArrayList<>();
    private final List<String> prerequisites = new ArrayList<>();

    public Course() {
    }

    public Course(int id, String code, String title, int credits, int capacity,
                  String semester, int instructorId) {
        this.id = id;
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.capacity = capacity;
        this.semester = semester;
        this.instructorId = instructorId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(int instructorId) {
        this.instructorId = instructorId;
    }

    public boolean hasInstructor() {
        return instructorId > 0;
    }

    public List<TimeSlot> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<TimeSlot> slots) {
        schedule.clear();
        if (slots != null) {
            schedule.addAll(slots);
        }
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<String> codes) {
        prerequisites.clear();
        if (codes != null) {
            prerequisites.addAll(codes);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Course)) {
            return false;
        }
        return id == ((Course) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return code + " - " + title;
    }
}
