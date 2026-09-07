package cms;

import cms.controller.AppController;
import cms.controller.CourseController;
import cms.controller.EnrollmentController;
import cms.controller.InstructorController;
import cms.controller.ReportController;
import cms.controller.StudentController;
import cms.model.SeedData;
import cms.model.dao.AdminDao;
import cms.model.dao.CourseDao;
import cms.model.dao.EnrollmentDao;
import cms.model.dao.InstructorDao;
import cms.model.dao.StudentDao;
import cms.model.dao.WaitlistDao;
import cms.model.service.AuthService;
import cms.model.service.CourseService;
import cms.model.service.EnrollmentService;
import cms.model.service.InstructorService;
import cms.model.service.ProgressReportService;
import cms.model.service.RegistrationRuleService;
import cms.model.service.ScheduleService;
import cms.model.service.StatisticsService;
import cms.model.service.StudentService;
import cms.model.service.WaitlistService;
import cms.model.service.history.OperationHistory;
import cms.view.ConsoleView;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();
        try {
            Path dataDir = Paths.get("data");
            run(view, dataDir);
        } catch (Throwable fatal) {
            view.error("Something went badly wrong: " + fatal.getMessage());
            logFatal(fatal);
            view.error("Full details are in error.log.");
            System.exit(1);
        }
    }

    private static void run(ConsoleView view, Path dataDir) {
        AdminDao adminDao = new AdminDao(dataDir.resolve("admins.csv"));
        InstructorDao instructorDao = new InstructorDao(dataDir.resolve("instructors.csv"));
        CourseDao courseDao = new CourseDao(dataDir.resolve("courses.csv"));
        StudentDao studentDao = new StudentDao(dataDir.resolve("students.csv"));
        EnrollmentDao enrollmentDao = new EnrollmentDao(dataDir.resolve("enrollments.csv"));
        WaitlistDao waitlistDao = new WaitlistDao(dataDir.resolve("waitlist.csv"));

        adminDao.load();
        instructorDao.load();
        courseDao.load();
        studentDao.load();
        enrollmentDao.load();
        waitlistDao.load();

        SeedData.ensureAdmin(adminDao);
        SeedData.seedIfEmpty(instructorDao, courseDao, studentDao, enrollmentDao);

        OperationHistory history = new OperationHistory();
        RegistrationRuleService ruleService = new RegistrationRuleService(enrollmentDao, courseDao);
        ScheduleService scheduleService = new ScheduleService(enrollmentDao, courseDao);
        WaitlistService waitlistService = new WaitlistService(waitlistDao, enrollmentDao, studentDao,
                ruleService, scheduleService);

        AuthService authService = new AuthService(adminDao);
        CourseService courseService = new CourseService(courseDao, instructorDao, enrollmentDao,
                ruleService, waitlistService, history);
        StudentService studentService = new StudentService(studentDao, enrollmentDao, waitlistDao, history);
        InstructorService instructorService = new InstructorService(instructorDao, courseDao);
        EnrollmentService enrollmentService = new EnrollmentService(enrollmentDao, waitlistDao, courseDao,
                studentDao, ruleService, scheduleService, waitlistService, history);
        StatisticsService statisticsService = new StatisticsService(courseDao, enrollmentDao, waitlistDao);
        ProgressReportService progressReportService =
                new ProgressReportService(enrollmentDao, courseDao, studentDao);

        CourseController courseController = new CourseController(view, courseService, instructorService,
                enrollmentService, waitlistService);
        StudentController studentController = new StudentController(view, studentService, scheduleService,
                progressReportService);
        InstructorController instructorController = new InstructorController(view, instructorService,
                scheduleService);
        EnrollmentController enrollmentController = new EnrollmentController(view, enrollmentService,
                waitlistService, courseService, studentService);
        ReportController reportController = new ReportController(view, statisticsService,
                progressReportService, courseService, studentService);

        Runnable saveAll = () -> {
            instructorDao.flush();
            courseDao.flush();
            studentDao.flush();
            enrollmentDao.flush();
            waitlistDao.flush();
        };

        AppController app = new AppController(view, authService, courseController, studentController,
                instructorController, enrollmentController, reportController, saveAll);
        app.run();
    }

    private static void logFatal(Throwable t) {
        try {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            String entry = "[" + LocalDateTime.now() + "] " + sw + System.lineSeparator();
            Files.write(Paths.get("error.log"), entry.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }
}
