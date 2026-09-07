package cms.model.dao;

import cms.model.dto.Enrollment;
import cms.model.dto.EnrollmentStatus;
import cms.util.CsvUtils;
import java.nio.file.Path;
import java.util.List;

public class EnrollmentDao extends CsvDao<Enrollment> {

    public EnrollmentDao(Path file) {
        super(file);
    }

    @Override
    protected String header() {
        return "id,studentId,courseId,semester,status,grade,timestamp";
    }

    @Override
    protected String toRow(Enrollment e) {
        return CsvUtils.toLine(
                String.valueOf(e.getId()),
                String.valueOf(e.getStudentId()),
                String.valueOf(e.getCourseId()),
                e.getSemester(),
                e.getStatus().name(),
                e.getGrade() == null ? "" : e.getGrade(),
                String.valueOf(e.getTimestamp()));
    }

    @Override
    protected Enrollment fromRow(List<String> f) {
        if (f.size() < 7) {
            throw new IllegalArgumentException("expected 7 columns, got " + f.size());
        }
        Enrollment e = new Enrollment();
        e.setId(Integer.parseInt(f.get(0).trim()));
        e.setStudentId(Integer.parseInt(f.get(1).trim()));
        e.setCourseId(Integer.parseInt(f.get(2).trim()));
        e.setSemester(f.get(3));
        e.setStatus(EnrollmentStatus.fromString(f.get(4)));
        e.setGrade(f.get(5));
        e.setTimestamp(Long.parseLong(f.get(6).trim()));
        return e;
    }

    @Override
    protected int idOf(Enrollment e) {
        return e.getId();
    }

    @Override
    protected void assignId(Enrollment e, int id) {
        e.setId(id);
    }

    @Override
    protected String entityName() {
        return "Enrollment";
    }
}
