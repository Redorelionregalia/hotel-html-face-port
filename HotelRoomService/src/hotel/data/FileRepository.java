package hotel.data;

import hotel.model.Entity;

import java.io.File;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * OOP: INHERITANCE + TEMPLATE METHOD.
 * All the file work (reading, writing, locking, adding, updating, removing) is written ONCE here.
 * Subclasses only say how ONE record is turned into text and back (fromFields / toFields).
 * MenuRepository and OrderRepository reuse everything else.
 */
public abstract class FileRepository<T extends Entity> implements Repository<T> {
    private final File file;
    private final StorageLock lock;

    protected FileRepository(File file, StorageLock lock) { this.file = file; this.lock = lock; }

    /** Subclass: build an object from the decoded text fields of one line. */
    protected abstract T fromFields(String[] fields);
    /** Subclass: turn an object into the text fields of one line. */
    protected abstract String[] toFields(T entity);

    protected boolean fileExists() { return file.exists(); }

    @Override public long lastModified() { return file.lastModified(); }

    @Override public List<T> findAll() {
        List<T> list = new ArrayList<>();
        if (!file.exists()) return list;
        try {
            for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) continue;
                try {
                    String[] f = line.split("\t", -1);
                    for (int i = 0; i < f.length; i++) f[i] = URLDecoder.decode(f[i], StandardCharsets.UTF_8);
                    list.add(fromFields(f));
                } catch (Exception damaged) { /* skip a damaged line */ }
            }
        } catch (Exception busy) { /* file is being written; the caller keeps its old data */ }
        return list;
    }

    @Override public T findById(int id) {
        for (T e : findAll()) if (e.getId() == id) return e;
        return null;
    }

    @Override public void add(T entity) {
        lock.run(() -> {
            List<T> all = findAll(); int next = 1;
            for (T e : all) next = Math.max(next, e.getId() + 1);
            entity.setId(next); all.add(entity); writeAll(all); return null;
        });
    }

    @Override public void update(T entity) {
        lock.run(() -> {
            List<T> all = findAll();
            for (int i = 0; i < all.size(); i++) if (all.get(i).getId() == entity.getId()) all.set(i, entity);
            writeAll(all); return null;
        });
    }

    @Override public void remove(int id) {
        lock.run(() -> { List<T> all = findAll(); all.removeIf(e -> e.getId() == id); writeAll(all); return null; });
    }

    private void writeAll(List<T> all) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (T e : all) {
            String[] f = toFields(e);
            for (int i = 0; i < f.length; i++) {
                if (i > 0) sb.append('\t');
                sb.append(URLEncoder.encode(f[i] == null ? "" : f[i], StandardCharsets.UTF_8));
            }
            sb.append('\n');
        }
        File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
        Files.write(tmp.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        try { Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (Exception e) { Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING); }
    }

    protected static String enc(String s) { return URLEncoder.encode(s, StandardCharsets.UTF_8); }
    protected static String dec(String s) { return URLDecoder.decode(s, StandardCharsets.UTF_8); }
}
