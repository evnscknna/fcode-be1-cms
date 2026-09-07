package cms.model.dao;

import cms.model.dto.WaitlistEntry;
import cms.util.CsvUtils;
import java.nio.file.Path;
import java.util.List;

public class WaitlistDao extends CsvDao<WaitlistEntry> {

    public WaitlistDao(Path file) {
        super(file);
    }

    @Override
    protected String header() {
        return "id,studentId,courseId,timestamp";
    }

    @Override
    protected String toRow(WaitlistEntry w) {
        return CsvUtils.toLine(
                String.valueOf(w.getId()),
                String.valueOf(w.getStudentId()),
                String.valueOf(w.getCourseId()),
                String.valueOf(w.getTimestamp()));
    }

    @Override
    protected WaitlistEntry fromRow(List<String> f) {
        if (f.size() < 4) {
            throw new IllegalArgumentException("expected 4 columns, got " + f.size());
        }
        WaitlistEntry w = new WaitlistEntry();
        w.setId(Integer.parseInt(f.get(0).trim()));
        w.setStudentId(Integer.parseInt(f.get(1).trim()));
        w.setCourseId(Integer.parseInt(f.get(2).trim()));
        w.setTimestamp(Long.parseLong(f.get(3).trim()));
        return w;
    }

    @Override
    protected int idOf(WaitlistEntry w) {
        return w.getId();
    }

    @Override
    protected void assignId(WaitlistEntry w, int id) {
        w.setId(id);
    }

    @Override
    protected String entityName() {
        return "Waitlist entry";
    }
}
