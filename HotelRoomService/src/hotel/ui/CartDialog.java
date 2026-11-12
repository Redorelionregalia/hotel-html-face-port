package hotel.ui;

import hotel.data.ShopException;
import hotel.data.ShopService;
import hotel.model.Cart;
import hotel.model.Item;
import hotel.model.Order;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * The cart window: unit price, quantity, line total and grand total, plus room number and notes.
 * OOP: it works on the Cart OBJECT (cart.setQuantity, cart.total) and asks ShopService to place the order;
 * it contains no business rules of its own.
 */
public class CartDialog extends JDialog {
    private final UiFactory ui;
    private final ShopService shop;
    private final Cart cart;
    private final List<Item> menu;
    private final Runnable onCartChanged;
    private final Consumer<Order> onOrderPlaced;
    private final JTextField room;
    private final JTextArea notes = new JTextArea(3, 20);
    private final JLabel message = new JLabel(" ");

    public CartDialog(JFrame owner, UiFactory ui, ShopService shop, Cart cart, List<Item> menu, String roomText,
                      Runnable onCartChanged, Consumer<Order> onOrderPlaced) {
        super(owner, "Your Cart", true);
        this.ui = ui; this.shop = shop; this.cart = cart; this.menu = menu; this.onCartChanged = onCartChanged; this.onOrderPlaced = onOrderPlaced;
        room = ui.textField(roomText, 10);
        notes.setLineWrap(true); notes.setWrapStyleWord(true); notes.setBackground(ui.c("color.surface"));
        notes.setForeground(ui.c("color.text")); notes.setBorder(UiFactory.pad(6, 8, 6, 8));
        message.setForeground(ui.c("color.bad")); message.setFont(ui.theme().body(Font.BOLD, ui.theme().baseSize() - 1));
        setSize(470, 680); setLocationRelativeTo(owner);
        rebuild();
    }

    private Item find(int id) { for (Item i : menu) if (i.getId() == id) return i; return null; }

    private void rebuild() {
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(ui.c("color.background"));
        JLabel title = ui.heading("Your Cart", 26, ui.c("color.text")); title.setBorder(UiFactory.pad(14, 18, 10, 18));
        root.add(title, BorderLayout.NORTH);

        JPanel lines = ui.vbox(); lines.setBorder(UiFactory.pad(0, 18, 0, 18));
        if (cart.isEmpty()) lines.add(UiFactory.left(ui.label("Your cart is empty.", Font.PLAIN, ui.theme().baseSize(), ui.c("color.muted"))));
        for (int id : cart.getQuantities().keySet()) {
            Item item = find(id); if (item == null) continue;
            int qty = cart.get(id);
            JPanel row = ui.transparent(new BorderLayout(10, 0)); row.setBorder(UiFactory.pad(8, 0, 8, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            row.add(new ItemPicture(ui, shop.images(), item, 52, 52, false), BorderLayout.WEST);
            JPanel mid = ui.vbox();
            mid.add(UiFactory.left(ui.label(item.getName(), Font.BOLD, ui.theme().baseSize(), ui.c("color.text"))));
            mid.add(UiFactory.left(ui.label(ui.money(item.getPrice()) + " each", Font.PLAIN, 12, ui.c("color.muted"))));
            JPanel ctl = ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
            ctl.add(new Stepper(ui, qty, 1, item.getStock(), q -> { cart.setQuantity(item, q); onCartChanged.run(); rebuild(); }));
            JButton remove = ui.button("Remove", UiFactory.Kind.GHOST);
            remove.setForeground(ui.c("color.bad")); remove.setBorder(UiFactory.pad(2, 6, 2, 6));
            remove.addActionListener(e -> { cart.remove(id); onCartChanged.run(); rebuild(); });
            ctl.add(remove); mid.add(UiFactory.left(ctl));
            row.add(mid, BorderLayout.CENTER);
            row.add(ui.label(ui.money(item.getPrice() * qty), Font.BOLD, ui.theme().baseSize(), ui.c("color.text")), BorderLayout.EAST);
            lines.add(UiFactory.left(row));
        }
        JPanel wrap = ui.transparent(new BorderLayout()); wrap.add(lines, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(wrap); sp.setBorder(null); sp.getViewport().setOpaque(false); sp.setOpaque(false);
        root.add(sp, BorderLayout.CENTER);

        JPanel foot = ui.vbox(); foot.setOpaque(true); foot.setBackground(ui.c("color.surface2")); foot.setBorder(UiFactory.pad(12, 18, 14, 18));
        JPanel total = ui.transparent(new BorderLayout());
        total.add(ui.heading("Total", 24, ui.c("color.text")), BorderLayout.WEST);
        total.add(ui.heading(ui.money(cart.total(menu)), 24, ui.c("color.text")), BorderLayout.EAST);
        foot.add(UiFactory.left(total)); foot.add(Box.createVerticalStrut(8));
        foot.add(UiFactory.left(ui.labeled("Room number", room))); foot.add(Box.createVerticalStrut(8));
        JScrollPane ns = new JScrollPane(notes); ns.setBorder(ui.line());
        foot.add(UiFactory.left(ui.labeled("Notes for the kitchen (optional)", ns))); foot.add(Box.createVerticalStrut(6));
        foot.add(UiFactory.left(message)); foot.add(Box.createVerticalStrut(6));
        JButton place = ui.button("Place order", UiFactory.Kind.ACCENT); place.setEnabled(!cart.isEmpty());
        place.addActionListener(e -> placeOrder());
        foot.add(UiFactory.left(place));
        root.add(foot, BorderLayout.SOUTH);
        setContentPane(root); revalidate(); repaint();
    }

    private void placeOrder() {
        try {
            Order order = shop.placeOrder(room.getText(), notes.getText().trim(), cart);
            cart.clear(); dispose(); onOrderPlaced.accept(order);
        } catch (ShopException ex) { message.setText(ex.getMessage()); }
    }
}
