package hotel.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Makes sure the Customer app and the Staff app never write the data files at the same moment.
 * It is re-entrant: code that already holds the lock can call other locked code.
 */
public class StorageLock {
    /** A piece of work that must run while the lock is held. */
    public interface Action<T> { T run() throws Exception; }

    private final File lockFile;
    private int depth = 0;

    public StorageLock(File lockFile) { this.lockFile = lockFile; }

    public synchronized <T> T run(Action<T> action) {
        if (depth > 0) return call(action);
        depth++;
        try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw"); FileChannel ch = raf.getChannel(); FileLock lk = ch.lock()) {
            return call(action);
        } catch (IOException e) {
            throw new ShopException("Could not access the data files.", e);
        } finally {
            depth--;
        }
    }

    private <T> T call(Action<T> action) {
        try { return action.run(); }
        catch (RuntimeException e) { throw e; }
        catch (Exception e) { throw new ShopException(e.getMessage(), e); }
    }
}
