package hotel.model;

/**
 * A menu item (food, drink or amenity).
 *
 * OOP: ENCAPSULATION - every field is private. The only way to change price or stock is through
 * methods that protect the rules (no negative price, cannot sell more than we have).
 * OOP: the class also owns its own behavior (isLowStock, canSupply, matches) instead of other classes
 * re-implementing those rules.
 */
public class Item implements Entity {
    private int id;
    private String name;
    private String description;
    private Category category;
    private String emoji;
    private String imageName;
    private double price;
    private int stock;

    /** Used when staff create a new item (id is given later by the repository). */
    public Item(String name, Category category, String description, double price, int stock, String emoji) {
        this(0, name, category, description, price, stock, emoji, "");
    }

    /** Used when an item is loaded from the data file. */
    public Item(int id, String name, Category category, String description, double price, int stock, String emoji, String imageName) {
        this.id = id; this.name = name; this.category = category; this.description = description;
        setPrice(price); setStock(stock);
        this.emoji = (emoji == null || emoji.isEmpty()) ? "\uD83C\uDF7D\uFE0F" : emoji;
        this.imageName = imageName == null ? "" : imageName;
    }

    @Override public int getId() { return id; }
    @Override public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public String getEmoji() { return emoji; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName == null ? "" : imageName; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }

    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
        this.stock = stock;
    }

    public boolean isInStock() { return stock > 0; }
    public boolean isLowStock() { return stock > 0 && stock <= 5; }
    public boolean canSupply(int quantity) { return quantity <= stock; }

    /** Takes items out of stock. Refuses if there is not enough, so stock can never go below zero. */
    public void reduceStock(int quantity) {
        if (!canSupply(quantity)) throw new IllegalStateException("Sorry, " + name + " is no longer available in that quantity.");
        stock -= quantity;
    }

    /** True if this item belongs to the category (null = all) and its name/description contains the search text. */
    public boolean matches(Category wanted, String search) {
        if (wanted != null && category != wanted) return false;
        return search == null || search.isEmpty() || (name + " " + description).toLowerCase().contains(search.toLowerCase());
    }
}
