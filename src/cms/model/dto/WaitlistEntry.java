package cms.model.dto;

import java.util.Objects;

public class WaitlistEntry {

    private int id;
    private int studentId;
    private int courseId;
    private long timestamp;

    public WaitlistEntry() {
    }

    public WaitlistEntry(int id, int studentId, int courseId, long timestamp) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WaitlistEntry)) {
            return false;
        }
        return id == ((WaitlistEntry) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
