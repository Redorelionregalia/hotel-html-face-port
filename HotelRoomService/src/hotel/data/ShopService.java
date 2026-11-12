package hotel.data;

import hotel.model.Cart;
import hotel.model.Item;
import hotel.model.Order;
import hotel.model.OrderLine;
import hotel.model.OrderStatus;

import java.util.List;
import java.util.Map;

/**
 * OOP: the single entry point ("facade") the screens talk to. It holds the business rules
 * (a real order needs a room, enough stock, ...) and coordinates the two repositories.
 * The screens never touch files directly.
 */
public class ShopService {
    private final Repository<Item> menu;
    private final Repository<Order> orders;
    private final StorageLock lock;
    private final ImageLibrary images;

    /** OOP: DEPENDENCY INJECTION - the parts are handed in, so this class does not care how they are built. */
    public ShopService(Repository<Item> menu, Repository<Order> orders, StorageLock lock, ImageLibrary images) {
        this.menu = menu; this.orders = orders; this.lock = lock; this.images = images;
    }

    public List<Item> getMenu() { return menu.findAll(); }
    public long menuVersion() { return menu.lastModified(); }
    public List<Order> getOrders() { return orders.findAll(); }
    public long ordersVersion() { return orders.lastModified(); }
    public Order findOrder(int id) { return orders.findById(id); }
    public ImageLibrary images() { return images; }

    /** Checks the whole cart first, then takes the stock and saves the order in one safe step. */
    public Order placeOrder(String room, String notes, Cart cart) {
        if (room == null || room.trim().isEmpty()) throw new ShopException("Please enter your room number.");
        if (cart.isEmpty()) throw new ShopException("Your cart is empty.");
        return lock.run(() -> {
            List<Item> items = menu.findAll();
            for (Map.Entry<Integer, Integer> e : cart.getQuantities().entrySet()) {
                Item it = find(items, e.getKey());
                if (it == null) throw new ShopException("An item in your cart is no longer on the menu.");
                if (!it.canSupply(e.getValue())) throw new ShopException("Sorry, " + it.getName() + " is no longer available in that quantity.");
            }
            Order order = new Order(room.trim(), notes);
            for (Map.Entry<Integer, Integer> e : cart.getQuantities().entrySet()) {
                Item it = find(items, e.getKey());
                it.reduceStock(e.getValue()); menu.update(it);
                order.addLine(new OrderLine(it.getName(), it.getPrice(), e.getValue()));
            }
            orders.add(order);
            return order;
        });
    }

    public void changeStatus(int orderId, OrderStatus status) {
        lock.run(() -> { Order o = orders.findById(orderId); if (o != null) { o.setStatus(status); orders.update(o); } return null; });
    }

    public void editItem(int id, double price, int stock) {
        lock.run(() -> { Item it = menu.findById(id); if (it != null) { it.setPrice(price); it.setStock(stock); menu.update(it); } return null; });
    }

    public void setItemImage(int id, String imageName) {
        lock.run(() -> { Item it = menu.findById(id); if (it != null) { it.setImageName(imageName); menu.update(it); } return null; });
    }

    public void addItem(Item item) { menu.add(item); }
    public void removeItem(int id) { menu.remove(id); }

    private static Item find(List<Item> list, int id) { for (Item i : list) if (i.getId() == id) return i; return null; }
}
