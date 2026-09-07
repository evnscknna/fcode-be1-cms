package cms.model.dao;

import cms.model.dto.Student;
import cms.util.CsvUtils;
import java.nio.file.Path;
import java.util.List;

public class StudentDao extends CsvDao<Student> {

    public StudentDao(Path file) {
        super(file);
    }

    @Override
    protected String header() {
        return "id,code,fullName,email,major,enrollmentYear";
    }

    @Override
    protected String toRow(Student s) {
        return CsvUtils.toLine(
                String.valueOf(s.getId()),
                s.getCode(),
                s.getFullName(),
                s.getEmail(),
                s.getMajor(),
                String.valueOf(s.getEnrollmentYear()));
    }

    @Override
    protected Student fromRow(List<String> f) {
        if (f.size() < 6) {
            throw new IllegalArgumentException("expected 6 columns, got " + f.size());
        }
        Student s = new Student();
        s.setId(Integer.parseInt(f.get(0).trim()));
        s.setCode(f.get(1));
        s.setFullName(f.get(2));
        s.setEmail(f.get(3));
        s.setMajor(f.get(4));
        s.setEnrollmentYear(Integer.parseInt(f.get(5).trim()));
        return s;
    }

    @Override
    protected int idOf(Student s) {
        return s.getId();
    }

    @Override
    protected void assignId(Student s, int id) {
        s.setId(id);
    }

    @Override
    protected String entityName() {
        return "Student";
    }
}
