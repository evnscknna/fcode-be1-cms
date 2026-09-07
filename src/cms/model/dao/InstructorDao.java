package cms.model.dao;

import cms.model.dto.Instructor;
import cms.util.CsvUtils;
import java.nio.file.Path;
import java.util.List;

public class InstructorDao extends CsvDao<Instructor> {

    public InstructorDao(Path file) {
        super(file);
    }

    @Override
    protected String header() {
        return "id,code,fullName,email,department";
    }

    @Override
    protected String toRow(Instructor i) {
        return CsvUtils.toLine(
                String.valueOf(i.getId()),
                i.getCode(),
                i.getFullName(),
                i.getEmail(),
                i.getDepartment());
    }

    @Override
    protected Instructor fromRow(List<String> f) {
        if (f.size() < 5) {
            throw new IllegalArgumentException("expected 5 columns, got " + f.size());
        }
        Instructor i = new Instructor();
        i.setId(Integer.parseInt(f.get(0).trim()));
        i.setCode(f.get(1));
        i.setFullName(f.get(2));
        i.setEmail(f.get(3));
        i.setDepartment(f.get(4));
        return i;
    }

    @Override
    protected int idOf(Instructor i) {
        return i.getId();
    }

    @Override
    protected void assignId(Instructor i, int id) {
        i.setId(id);
    }

    @Override
    protected String entityName() {
        return "Instructor";
    }
}
