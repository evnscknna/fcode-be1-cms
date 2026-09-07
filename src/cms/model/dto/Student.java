package cms.model.dto;

import java.util.Objects;

public class Student {

    private int id;
    private String code;
    private String fullName;
    private String email;
    private String major;
    private int enrollmentYear;

    public Student() {
    }

    public Student(int id, String code, String fullName, String email,
                   String major, int enrollmentYear) {
        this.id = id;
        this.code = code;
        this.fullName = fullName;
        this.email = email;
        this.major = major;
        this.enrollmentYear = enrollmentYear;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public int getEnrollmentYear() {
        return enrollmentYear;
    }

    public void setEnrollmentYear(int enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Student)) {
            return false;
        }
        return id == ((Student) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return code + " - " + fullName;
    }
}
