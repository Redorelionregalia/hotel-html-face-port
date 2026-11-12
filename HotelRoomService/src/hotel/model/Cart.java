package hotel.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The guest's shopping cart. OOP: the cart OWNS its rules (never more than the stock, totals,
 * clean-up when staff change the menu), so the screens just call these methods.
 */
public class Cart {
    private final Map<Integer, Integer> quantities = new LinkedHashMap<>();

    /** Adds up to {@code quantity} of the item, limited by stock. Returns how many were really added. */
    public int add(Item item, int quantity) {
        int have = get(item.getId());
        int allowed = Math.min(quantity, item.getStock() - have);
        if (allowed <= 0) return 0;
        quantities.put(item.getId(), have + allowed);
        return allowed;
    }

    public void setQuantity(Item item, int quantity) {
        int q = Math.min(quantity, item.getStock());
        if (q < 1) quantities.remove(item.getId()); else quantities.put(item.getId(), q);
    }

    public int get(int itemId) { return quantities.getOrDefault(itemId, 0); }
    public void remove(int itemId) { quantities.remove(itemId); }
    public void clear() { quantities.clear(); }
    public boolean isEmpty() { return quantities.isEmpty(); }
    public Map<Integer, Integer> getQuantities() { return Collections.unmodifiableMap(quantities); }

    /** Total number of pieces in the cart. */
    public int count() { int n = 0; for (int q : quantities.values()) n += q; return n; }

    /** Price of the whole cart, using the current prices on the menu. */
    public double total(List<Item> menu) {
        double t = 0;
        for (Item i : menu) t += i.getPrice() * get(i.getId());
        return t;
    }

    /** Removes items that were deleted or sold out and lowers quantities that are now above the stock. */
    public void syncWith(List<Item> menu) {
        for (Iterator<Map.Entry<Integer, Integer>> it = quantities.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Integer, Integer> e = it.next();
            Item found = null;
            for (Item m : menu) if (m.getId() == e.getKey()) found = m;
            if (found == null || !found.isInStock()) it.remove();
            else if (e.getValue() > found.getStock()) e.setValue(found.getStock());
        }
    }
}
