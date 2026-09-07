package cms.model.dto;

import java.util.Objects;

// Rows are never deleted; a drop just flips the status. This file is the history.
public class Enrollment {

    private int id;
    private int studentId;
    private int courseId;
    private String semester;
    private EnrollmentStatus status;
    private String grade;
    private long timestamp;

    public Enrollment() {
    }

    public Enrollment(int id, int studentId, int courseId, String semester,
                      EnrollmentStatus status, String grade, long timestamp) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.semester = semester;
        this.status = status;
        this.grade = grade;
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

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
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
        if (!(o instanceof Enrollment)) {
            return false;
        }
        return id == ((Enrollment) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
