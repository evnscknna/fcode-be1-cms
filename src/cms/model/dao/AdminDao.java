package cms.model.dao;

import cms.model.dto.Admin;
import cms.util.CsvUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

// Separate from CsvDao: admins are keyed by username, not an int id.
public class AdminDao {

    private final Path file;
    private final List<Admin> cache = new ArrayList<>();

    public AdminDao(Path file) {
        this.file = file;
    }

    public void load() {
        cache.clear();
        try {
            if (!Files.exists(file)) {
                if (file.getParent() != null) {
                    Files.createDirectories(file.getParent());
                }
                writeAll();
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) {
                    continue;
                }
                List<String> f = CsvUtils.parseLine(line);
                if (f.size() < 2) {
                    throw new IllegalStateException("Bad row in " + file.getFileName()
                            + " at line " + (i + 1) + ": need 2 columns");
                }
                cache.add(new Admin(f.get(0), f.get(1)));
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot read " + file.getFileName(), ex);
        }
    }

    public Admin findByUsername(String username) {
        for (Admin a : cache) {
            if (a.getUsername().equals(username)) {
                return a;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public void insert(Admin admin) {
        cache.add(admin);
        writeAll();
    }

    private void writeAll() {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                w.write("username,password");
                w.write('\n');
                for (Admin a : cache) {
                    w.write(CsvUtils.toLine(a.getUsername(), a.getPassword()));
                    w.write('\n');
                }
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot write " + file.getFileName(), ex);
        }
    }
}
