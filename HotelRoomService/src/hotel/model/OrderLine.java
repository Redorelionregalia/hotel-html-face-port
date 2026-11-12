package hotel.model;

/** One line of an order, e.g. "2 x Club Sandwich @ 220". Immutable: once created it never changes. */
public class OrderLine {
    private final String name;
    private final double unitPrice;
    private final int quantity;

    public OrderLine(String name, double unitPrice, int quantity) {
        this.name = name; this.unitPrice = unitPrice; this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return unitPrice * quantity; }
}
