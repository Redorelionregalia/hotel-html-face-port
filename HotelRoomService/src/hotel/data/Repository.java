package hotel.data;

import hotel.model.Entity;
import java.util.List;

/**
 * OOP: ABSTRACTION (interface). Says WHAT a data store can do (find, add, update, remove)
 * without saying HOW. Today it is a text file; it could be a database later with no change to the screens.
 */
public interface Repository<T extends Entity> {
    List<T> findAll();
    T findById(int id);
    void add(T entity);
    void update(T entity);
    void remove(int id);
    /** Changes whenever the stored data changes (the apps use it to refresh themselves). */
    long lastModified();
}
