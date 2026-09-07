package cms.model.dao;

import cms.model.dto.Course;
import cms.model.dto.TimeSlot;
import cms.util.CsvUtils;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CourseDao extends CsvDao<Course> {

    public CourseDao(Path file) {
        super(file);
    }

    @Override
    protected String header() {
        return "id,code,title,credits,capacity,semester,instructorId,schedule,prerequisites";
    }

    @Override
    protected String toRow(Course c) {
        List<String> schedule = new ArrayList<>();
        for (TimeSlot slot : c.getSchedule()) {
            schedule.add(slot.format());
        }
        return CsvUtils.toLine(
                String.valueOf(c.getId()),
                c.getCode(),
                c.getTitle(),
                String.valueOf(c.getCredits()),
                String.valueOf(c.getCapacity()),
                c.getSemester(),
                String.valueOf(c.getInstructorId()),
                CsvUtils.joinSub(schedule),
                CsvUtils.joinSub(c.getPrerequisites()));
    }

    @Override
    protected Course fromRow(List<String> f) {
        if (f.size() < 9) {
            throw new IllegalArgumentException("expected 9 columns, got " + f.size());
        }
        Course c = new Course();
        c.setId(Integer.parseInt(f.get(0).trim()));
        c.setCode(f.get(1));
        c.setTitle(f.get(2));
        c.setCredits(Integer.parseInt(f.get(3).trim()));
        c.setCapacity(Integer.parseInt(f.get(4).trim()));
        c.setSemester(f.get(5));
        c.setInstructorId(Integer.parseInt(f.get(6).trim()));
        List<TimeSlot> slots = new ArrayList<>();
        for (String token : CsvUtils.splitSub(f.get(7))) {
            slots.add(TimeSlot.parse(token));
        }
        c.setSchedule(slots);
        c.setPrerequisites(CsvUtils.splitSub(f.get(8)));
        return c;
    }

    @Override
    protected int idOf(Course c) {
        return c.getId();
    }

    @Override
    protected void assignId(Course c, int id) {
        c.setId(id);
    }

    @Override
    protected String entityName() {
        return "Course";
    }
}
