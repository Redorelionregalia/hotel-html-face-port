package hotel.model;

/**
 * OOP: ABSTRACTION (interface).
 * Anything that is saved in a file and has a number (id) implements this.
 * The data layer only needs to know "it has an id" - not whether it is an Item or an Order.
 */
public interface Entity {
    int getId();
    void setId(int id);
}
