package cms.model.report;

import java.util.ArrayList;
import java.util.List;

public final class ProgressReport {

    public static final class Line {
        private final String courseCode;
        private final String courseTitle;
        private final int credits;
        private final String status;
        private final String grade;

        public Line(String courseCode, String courseTitle, int credits, String status, String grade) {
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.credits = credits;
            this.status = status;
            this.grade = grade;
        }

        public String getCourseCode() {
            return courseCode;
        }

        public String getCourseTitle() {
            return courseTitle;
        }

        public int getCredits() {
            return credits;
        }

        public String getStatus() {
            return status;
        }

        public String getGrade() {
            return grade;
        }
    }

    public static final class SemesterBlock {
        private final String semester;
        private final List<Line> lines = new ArrayList<>();

        public SemesterBlock(String semester) {
            this.semester = semester;
        }

        public String getSemester() {
            return semester;
        }

        public List<Line> getLines() {
            return lines;
        }
    }

    private final int studentId;
    private final String studentName;
    private final int completedCredits;
    private final int inProgressCredits;
    private final int targetCredits;
    private final double gpa;
    private final List<SemesterBlock> semesters;

    public ProgressReport(int studentId, String studentName, int completedCredits,
                          int inProgressCredits, int targetCredits, double gpa,
                          List<SemesterBlock> semesters) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.completedCredits = completedCredits;
        this.inProgressCredits = inProgressCredits;
        this.targetCredits = targetCredits;
        this.gpa = gpa;
        this.semesters = semesters;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getCompletedCredits() {
        return completedCredits;
    }

    public int getInProgressCredits() {
        return inProgressCredits;
    }

    public int getTargetCredits() {
        return targetCredits;
    }

    public int getRemainingCredits() {
        return Math.max(0, targetCredits - completedCredits);
    }

    public double getCompletionPct() {
        return targetCredits == 0 ? 0.0 : (completedCredits * 100.0) / targetCredits;
    }

    public boolean hasGpa() {
        return gpa >= 0;
    }

    public double getGpa() {
        return gpa;
    }

    public List<SemesterBlock> getSemesters() {
        return semesters;
    }
}
