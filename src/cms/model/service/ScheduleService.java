package cms.model.service;

import cms.model.dao.CourseDao;
import cms.model.dao.EnrollmentDao;
import cms.model.dto.Course;
import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.model.dto.TimeSlot;
import cms.model.report.ScheduleRow;
import cms.util.SortUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ScheduleService {

    private final EnrollmentDao enrollmentDao;
    private final CourseDao courseDao;

    public ScheduleService(EnrollmentDao enrollmentDao, CourseDao courseDao) {
        this.enrollmentDao = enrollmentDao;
        this.courseDao = courseDao;
    }

    public List<ScheduleRow> studentSchedule(int studentId) {
        List<ScheduleRow> rows = new ArrayList<>();
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getStudentId() != studentId || e.getStatus() != EnrollmentStatus.ENROLLED) {
                continue;
            }
            Course c = courseDao.findById(e.getCourseId());
            if (c == null) {
                continue;
            }
            for (TimeSlot slot : c.getSchedule()) {
                rows.add(new ScheduleRow(slot, c.getCode(), c.getTitle(), c.getSemester()));
            }
        }
        SortUtils.mergeSort(rows, Comparator.naturalOrder());
        return rows;
    }

    public List<ScheduleRow> instructorSchedule(int instructorId) {
        List<ScheduleRow> rows = new ArrayList<>();
        for (Course c : courseDao.findAll()) {
            if (c.getInstructorId() != instructorId) {
                continue;
            }
            for (TimeSlot slot : c.getSchedule()) {
                rows.add(new ScheduleRow(slot, c.getCode(), c.getTitle(), c.getSemester()));
            }
        }
        SortUtils.mergeSort(rows, Comparator.naturalOrder());
        return rows;
    }

    public void assertNoConflict(int studentId, Course target) {
        for (Enrollment e : enrollmentDao.findAll()) {
            if (e.getStudentId() != studentId || e.getStatus() != EnrollmentStatus.ENROLLED) {
                continue;
            }
            Course c = courseDao.findById(e.getCourseId());
            if (c == null || c.getId() == target.getId()
                    || !Objects.equals(c.getSemester(), target.getSemester())) {
                continue;
            }
            for (TimeSlot a : target.getSchedule()) {
                for (TimeSlot b : c.getSchedule()) {
                    if (a.overlaps(b)) {
                        throw new IllegalArgumentException(target.getCode() + " (" + a.format()
                                + ") clashes with " + c.getCode() + " (" + b.format() + ").");
                    }
                }
            }
        }
    }
}
