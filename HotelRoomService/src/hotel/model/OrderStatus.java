package hotel.model;

/** OOP: ENUM with behavior - each status knows its label and its position in the progress bar. */
public enum OrderStatus {
    PENDING("Received"), PROCESSING("Preparing"), ON_THE_WAY("On the way"), COMPLETED("Delivered"), REJECTED("Rejected");

    private final String label;

    OrderStatus(String label) { this.label = label; }

    public String getLabel() { return label; }

    /** Position 0-3 in the progress bar, or -1 when the order was rejected. */
    public int step() { return this == REJECTED ? -1 : ordinal(); }

    public static OrderStatus parse(String text) {
        try { return valueOf(text); } catch (Exception e) { return PENDING; }
    }
}
