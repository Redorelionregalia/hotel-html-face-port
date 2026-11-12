package hotel.app;

import hotel.data.ShopService;
import hotel.model.Cart;
import hotel.model.Category;
import hotel.model.Item;
import hotel.model.Order;
import hotel.ui.BaseApp;
import hotel.ui.CartDialog;
import hotel.ui.MenuCard;
import hotel.ui.OrderCard;
import hotel.ui.UiFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Guest app. OOP: INHERITANCE - extends BaseApp and only fills in what is specific to guests. */
public class CustomerApp extends BaseApp {
    private final Cart cart = new Cart();
    private final Map<Integer, Integer> chosenQty = new HashMap<>();
    private List<Item> menu = new ArrayList<>();
    private Category categoryFilter = null;     // null = all categories
    private String search = "";
    private int trackedOrderId = -1, lastColumns = 0;
    private long menuVersion = -1, ordersVersion = -1;
    private boolean cartOpen = false;

    private final JTextField roomField, searchField, trackField;
    private final JPanel gridHolder = new JPanel(new BorderLayout()), trackOutput;
    private final JLabel banner, trackMessage;
    private final CardLayout pages = new CardLayout();
    private final JPanel content = new JPanel(pages);
    private JScrollPane scroll;
    private JButton navMenu, navTrack, cartButton;

    public static void main(String[] args) {
        AppContext ctx = AppContext.create();
        SwingUtilities.invokeLater(() -> { ctx.ui().installLookAndFeel(); new CustomerApp(ctx.shop(), ctx.ui()).launch(); });
    }

    public CustomerApp(ShopService shop, UiFactory ui) {
        super(shop, ui);
        roomField = ui.textField("501", 8); searchField = ui.textField("", 14); trackField = ui.textField("", 8);
        trackOutput = ui.vbox();
        banner = messageLabel(ui.c("color.ok")); trackMessage = messageLabel(ui.c("color.bad"));
    }

    @Override protected String windowTitle() { return "In-Room Dining"; }
    @Override protected String badge() { return ui.theme().text("hotel.tagline"); }

    @Override protected JComponent buildNav() {
        navMenu = navButton("Menu"); navTrack = navButton("Track Order"); cartButton = ui.button("Cart (0)", UiFactory.Kind.ACCENT);
        navMenu.addActionListener(e -> showPage("menu")); navTrack.addActionListener(e -> showPage("track"));
        cartButton.addActionListener(e -> openCart());
        return navPanel(navMenu, navTrack, cartButton);
    }

    @Override protected JComponent buildBody() {
        content.setOpaque(false); content.add(menuPage(), "menu"); content.add(trackPage(), "track");
        showPage("menu");
        return content;
    }

    private void showPage(String name) {
        pages.show(content, name);
        if (name.equals("menu")) highlight(navMenu, navTrack); else highlight(navTrack, navMenu);
        if (name.equals("track")) renderTrack();
    }

    // ================= menu page =================
    private JPanel menuPage() {
        JPanel page = ui.transparent(new BorderLayout());
        JPanel top = ui.vbox(); top.setBorder(UiFactory.pad(16, 18, 4, 18));
        top.add(UiFactory.left(ui.heading(ui.theme().text("text.menuTitle"), 30, ui.c("color.text"))));
        top.add(UiFactory.left(ui.label(ui.theme().text("text.menuSubtitle"), Font.PLAIN, ui.theme().baseSize(), ui.c("color.muted"))));
        banner.setBorder(UiFactory.pad(8, 0, 0, 0)); top.add(UiFactory.left(banner));

        JComboBox<String> categoryBox = new JComboBox<>();
        categoryBox.addItem("All categories");
        for (Category c : Category.values()) categoryBox.addItem(c.getLabel());
        categoryBox.addActionListener(e -> {
            int i = categoryBox.getSelectedIndex(); categoryFilter = i <= 0 ? null : Category.values()[i - 1]; renderGrid();
        });
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            private void changed() { search = searchField.getText().trim(); renderGrid(); }
            public void insertUpdate(DocumentEvent e) { changed(); }
            public void removeUpdate(DocumentEvent e) { changed(); }
            public void changedUpdate(DocumentEvent e) { changed(); }
        });
        JButton reset = ui.button("Reset filters", UiFactory.Kind.GHOST);
        reset.addActionListener(e -> { categoryBox.setSelectedIndex(0); searchField.setText(""); });
        JPanel resetWrap = ui.vbox(); resetWrap.add(Box.createVerticalStrut(14)); resetWrap.add(reset);
        JPanel bar = ui.transparent(new FlowLayout(FlowLayout.LEFT, 12, 8));
        bar.add(ui.labeled("Room number", roomField)); bar.add(ui.labeled("Category", categoryBox));
        bar.add(ui.labeled("Search", searchField)); bar.add(resetWrap);
        top.add(UiFactory.left(bar));
        page.add(top, BorderLayout.NORTH);

        gridHolder.setOpaque(false); gridHolder.setBorder(UiFactory.pad(0, 18, 18, 18));
        scroll = new JScrollPane(gridHolder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null); scroll.getViewport().setOpaque(false); scroll.setOpaque(false); scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { if (columnsNow() != lastColumns) renderGrid(); }
        });
        page.add(scroll, BorderLayout.CENTER);
        return page;
    }

    private int columnsNow() { return Math.max(1, (scroll.getViewport().getWidth() - 36 + 16) / 246); }

    private void renderGrid() {
        int cols = columnsNow(); lastColumns = cols;
        JPanel grid = ui.transparent(new GridLayout(0, cols, 16, 16));
        int shown = 0;
        for (Item item : menu) {
            if (!item.matches(categoryFilter, search)) continue;
            grid.add(new MenuCard(ui, shop.images(), item, cart.get(item.getId()), chosenQty.getOrDefault(item.getId(), 1),
                q -> chosenQty.put(item.getId(), q), q -> addToCart(item, q)));
            shown++;
        }
        for (int k = shown % cols; k > 0 && k < cols; k++) grid.add(ui.transparent(new BorderLayout()));
        gridHolder.removeAll();
        if (shown == 0) gridHolder.add(ui.label("No items match.", Font.PLAIN, ui.theme().baseSize(), ui.c("color.muted")), BorderLayout.NORTH);
        else gridHolder.add(grid, BorderLayout.NORTH);
        gridHolder.revalidate(); gridHolder.repaint();
    }

    private void addToCart(Item item, int quantity) {
        int added = cart.add(item, quantity);
        chosenQty.put(item.getId(), 1); banner.setText(" ");
        updateCartButton(); renderGrid();
        if (added > 0) openCart();
    }

    private void updateCartButton() { cartButton.setText("Cart (" + cart.count() + ")"); }

    // ================= cart =================
    private void openCart() {
        cartOpen = true;
        CartDialog dialog = new CartDialog(this, ui, shop, cart, menu, roomField.getText(), this::updateCartButton, this::orderPlaced);
        dialog.setVisible(true);
        cartOpen = false; updateCartButton(); renderGrid();
    }

    private void orderPlaced(Order order) {
        trackedOrderId = order.getId(); roomField.setText(order.getRoom());
        trackField.setText(String.valueOf(order.getId()));
        banner.setText("Thank you! Order #" + order.getId() + " for room " + order.getRoom() + " totals "
            + ui.money(order.getTotal()) + ". Track it under Track Order.");
        menuVersion = -1; ordersVersion = -1;
    }

    // ================= track page =================
    private JPanel trackPage() {
        JPanel page = ui.transparent(new BorderLayout());
        JPanel top = ui.vbox(); top.setBorder(UiFactory.pad(16, 18, 8, 18));
        top.add(UiFactory.left(ui.heading(ui.theme().text("text.trackTitle"), 30, ui.c("color.text"))));
        top.add(UiFactory.left(ui.label("Enter the order number you received when you placed your order.", Font.PLAIN, ui.theme().baseSize(), ui.c("color.muted"))));
        JButton go = ui.button("Check status", UiFactory.Kind.DARK);
        Runnable check = () -> {
            String s = trackField.getText().trim();
            if (!s.matches("\\d+")) { trackedOrderId = -1; trackMessage.setText("Order numbers are digits only."); }
            else { trackedOrderId = Integer.parseInt(s); trackMessage.setText(" "); }
            renderTrack();
        };
        go.addActionListener(e -> check.run()); trackField.addActionListener(e -> check.run());
        JPanel goWrap = ui.vbox(); goWrap.add(Box.createVerticalStrut(14)); goWrap.add(go);
        JPanel bar = ui.transparent(new FlowLayout(FlowLayout.LEFT, 12, 10));
        bar.add(ui.labeled("Order number", trackField)); bar.add(goWrap);
        top.add(UiFactory.left(bar)); top.add(UiFactory.left(trackMessage));
        page.add(top, BorderLayout.NORTH);

        JPanel holder = ui.transparent(new BorderLayout()); holder.setBorder(UiFactory.pad(0, 18, 18, 18));
        holder.add(trackOutput, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(holder); sp.setBorder(null); sp.getViewport().setOpaque(false); sp.setOpaque(false);
        page.add(sp, BorderLayout.CENTER);
        return page;
    }

    private void renderTrack() {
        trackOutput.removeAll();
        if (trackedOrderId > 0) {
            Order order = shop.findOrder(trackedOrderId);
            if (order == null) trackMessage.setText("No order found with that number.");
            else { trackMessage.setText(" "); trackOutput.add(UiFactory.left(new OrderCard(ui, order))); }
        }
        trackOutput.revalidate(); trackOutput.repaint();
    }

    // ================= auto refresh (staff edits appear by themselves) =================
    @Override protected void refresh() {
        long mv = shop.menuVersion();
        if (mv != menuVersion && !cartOpen) {
            List<Item> fresh = shop.getMenu();
            if (!fresh.isEmpty()) {
                menuVersion = mv; menu = fresh; cart.syncWith(menu);
                updateCartButton(); renderGrid();
            }
        }
        long ov = shop.ordersVersion();
        if (ov != ordersVersion) { ordersVersion = ov; if (trackedOrderId > 0) renderTrack(); }
    }
}
