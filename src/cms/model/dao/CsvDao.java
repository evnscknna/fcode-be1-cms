package cms.model.dao;

import cms.util.CsvUtils;
import cms.util.IdGenerator;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class CsvDao<T> {

    private final Path file;
    private final List<T> cache = new ArrayList<>();
    private IdGenerator ids = new IdGenerator(1);

    protected CsvDao(Path file) {
        this.file = file;
    }

    protected abstract String header();

    protected abstract String toRow(T entity);

    protected abstract T fromRow(List<String> fields);

    protected abstract int idOf(T entity);

    protected abstract void assignId(T entity, int id);

    protected abstract String entityName();

    public final void load() {
        cache.clear();
        try {
            if (!Files.exists(file)) {
                ensureParent();
                writeAll();
            }
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) { // line 0 is the header
                String line = lines.get(i);
                if (line.trim().isEmpty()) {
                    continue;
                }
                try {
                    cache.add(fromRow(CsvUtils.parseLine(line)));
                } catch (RuntimeException ex) {
                    throw new IllegalStateException("Bad row in " + file.getFileName()
                            + " at line " + (i + 1) + ": " + ex.getMessage(), ex);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot read " + file.getFileName(), ex);
        }
        List<Integer> existing = new ArrayList<>();
        for (T entity : cache) {
            existing.add(idOf(entity));
        }
        ids = IdGenerator.seededFrom(existing);
    }

    public final void flush() {
        writeAll();
    }

    public List<T> findAll() {
        return new ArrayList<>(cache);
    }

    public T findById(int id) {
        for (T entity : cache) {
            if (idOf(entity) == id) {
                return entity;
            }
        }
        return null;
    }

    public T getById(int id) {
        T found = findById(id);
        if (found == null) {
            throw new IllegalArgumentException(entityName() + " not found: " + id);
        }
        return found;
    }

    public boolean existsById(int id) {
        return findById(id) != null;
    }

    public int count() {
        return cache.size();
    }

    public T insert(T entity) {
        assignId(entity, ids.nextId());
        cache.add(entity);
        writeAll();
        return entity;
    }

    public void update(T entity) {
        int id = idOf(entity);
        for (int i = 0; i < cache.size(); i++) {
            if (idOf(cache.get(i)) == id) {
                cache.set(i, entity);
                writeAll();
                return;
            }
        }
        throw new IllegalArgumentException(entityName() + " not found: " + id);
    }

    public void deleteById(int id) {
        Iterator<T> it = cache.iterator();
        while (it.hasNext()) {
            if (idOf(it.next()) == id) {
                it.remove();
                writeAll();
                return;
            }
        }
        throw new IllegalArgumentException(entityName() + " not found: " + id);
    }

    protected final void writeAll() {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            ensureParent();
            try (BufferedWriter w = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                w.write(header());
                w.write('\n'); // always LF, so files are byte-identical on every OS
                for (T entity : cache) {
                    w.write(toRow(entity));
                    w.write('\n');
                }
            }
            try {
                // write temp then rename, so a crash cannot leave a half file
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException notAtomic) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot write " + file.getFileName(), ex);
        }
    }

    private void ensureParent() throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
