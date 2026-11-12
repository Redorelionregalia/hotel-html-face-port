package hotel.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A placed order. OOP: COMPOSITION - an Order "has" OrderLines and an OrderStatus.
 * ENCAPSULATION - the list of lines is private; outsiders only get a read-only view.
 */
public class Order implements Entity {
    private int id;
    private final String room;
    private final String notes;
    private OrderStatus status;
    private final long createdAt;
    private long updatedAt;
    private final List<OrderLine> lines = new ArrayList<>();

    public Order(String room, String notes) {
        this(0, room, notes, OrderStatus.PENDING, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public Order(int id, String room, String notes, OrderStatus status, long createdAt, long updatedAt) {
        this.id = id; this.room = room; this.notes = notes; this.status = status;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public String getRoom() { return room; }
    public String getNotes() { return notes; }
    public OrderStatus getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    public void setStatus(OrderStatus status) { this.status = status; this.updatedAt = System.currentTimeMillis(); }
    public void addLine(OrderLine line) { lines.add(line); }
    public List<OrderLine> getLines() { return Collections.unmodifiableList(lines); }

    public double getTotal() {
        double t = 0;
        for (OrderLine l : lines) t += l.getTotal();
        return t;
    }
}
