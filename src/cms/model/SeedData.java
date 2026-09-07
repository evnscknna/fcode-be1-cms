package cms.model;

import cms.model.dao.AdminDao;
import cms.model.dao.CourseDao;
import cms.model.dao.EnrollmentDao;
import cms.model.dao.InstructorDao;
import cms.model.dao.StudentDao;
import cms.model.dto.Admin;
import cms.model.dto.Course;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.model.dto.Instructor;
import cms.model.dto.Student;
import cms.model.dto.TimeSlot;
import java.util.Arrays;

// First-run sample data. Only runs when the files are empty.
public final class SeedData {

    private SeedData() {
    }

    public static void ensureAdmin(AdminDao adminDao) {
        if (adminDao.isEmpty()) {
            adminDao.insert(new Admin("admin", "admin123"));
        }
    }

    public static void seedIfEmpty(InstructorDao instructorDao, CourseDao courseDao,
                                   StudentDao studentDao, EnrollmentDao enrollmentDao) {
        if (instructorDao.count() > 0 || courseDao.count() > 0
                || studentDao.count() > 0 || enrollmentDao.count() > 0) {
            return;
        }

        Instructor turing = instructorDao.insert(new Instructor(0, "INS01", "Alan Turing",
                "alan.turing@univ.edu", "Computer Science"));
        Instructor lovelace = instructorDao.insert(new Instructor(0, "INS02", "Ada Lovelace",
                "ada.lovelace@univ.edu", "Mathematics"));

        Course cs101 = course("CS101", "Introduction to Programming", 3, 2, "2026-FALL",
                turing.getId(), "MON 09:00-10:30;WED 09:00-10:30");
        courseDao.insert(cs101);

        Course cs102 = course("CS102", "Data Structures", 3, 30, "2026-FALL",
                turing.getId(), "TUE 09:00-10:30;THU 09:00-10:30");
        cs102.setPrerequisites(Arrays.asList("CS101"));
        courseDao.insert(cs102);

        Course math101 = course("MATH101", "Calculus I", 4, 40, "2026-FALL",
                lovelace.getId(), "MON 11:00-12:30;WED 11:00-12:30");
        courseDao.insert(math101);

        Course cs201 = course("CS201", "Algorithms", 3, 1, "2026-FALL",
                turing.getId(), "MON 09:00-10:30;FRI 09:00-10:30");
        cs201.setPrerequisites(Arrays.asList("CS101"));
        courseDao.insert(cs201);

        studentDao.insert(new Student(0, "SE1701", "Alice Nguyen",
                "alice@student.edu", "Software Engineering", 2026));
        studentDao.insert(new Student(0, "SE1702", "Bob Tran",
                "bob@student.edu", "Software Engineering", 2026));
        Student charlie = studentDao.insert(new Student(0, "SE1703", "Charlie Le",
                "charlie@student.edu", "Software Engineering", 2025));

        // Charlie already passed CS101 -> meets the CS201/CS102 prerequisite.
        enrollmentDao.insert(new Enrollment(0, charlie.getId(), cs101.getId(), "2025-FALL",
                EnrollmentStatus.COMPLETED, "B+", System.currentTimeMillis() - 86_400_000L));
    }

    private static Course course(String code, String title, int credits, int capacity,
                                 String semester, int instructorId, String schedule) {
        Course c = new Course(0, code, title, credits, capacity, semester, instructorId);
        for (String token : schedule.split(";")) {
            c.getSchedule().add(TimeSlot.parse(token.trim()));
        }
        return c;
    }
}
