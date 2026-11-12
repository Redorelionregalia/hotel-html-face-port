package hotel.model;

/** OOP: ENUM - a fixed set of allowed values with their own data (the label), instead of loose strings. */
public enum Category {
    AMENITY("Amenities"), FOOD("Dining"), BEVERAGE("Beverages");

    private final String label;

    Category(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static Category parse(String text) {
        try { return valueOf(text); } catch (Exception e) { return FOOD; }
    }
}
