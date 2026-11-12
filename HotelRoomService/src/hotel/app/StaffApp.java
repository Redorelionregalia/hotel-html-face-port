package hotel.app;

import hotel.data.ShopService;
import hotel.model.Item;
import hotel.model.Order;
import hotel.model.OrderStatus;
import hotel.ui.AddItemForm;
import hotel.ui.BaseApp;
import hotel.ui.ItemEditorRow;
import hotel.ui.StaffOrderCard;
import hotel.ui.UiFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Staff app. OOP: INHERITANCE - extends BaseApp; the order cards and item rows are separate objects. */
public class StaffApp extends BaseApp {
    private final CardLayout screens = new CardLayout(), tabs = new CardLayout();
    private final JPanel root = new JPanel(screens), tabPanel = new JPanel(tabs);
    private final JPanel ordersList, chips, menuList;
    private final JLabel banner;
    private OrderStatus statusFilter = null;    // null = all
    private long ordersVersion = -1;
    private boolean signedIn = false;
    private JButton navOrders, navMenu;

    public static void main(String[] args) {
        AppContext ctx = AppContext.create();
        SwingUtilities.invokeLater(() -> { ctx.ui().installLookAndFeel(); new StaffApp(ctx.shop(), ctx.ui()).launch(); });
    }

    public StaffApp(ShopService shop, UiFactory ui) {
        super(shop, ui);
        ordersList = ui.vbox(); menuList = ui.vbox();
        chips = ui.transparent(new FlowLayout(FlowLayout.LEFT, 6, 6));
        banner = messageLabel(ui.c("color.ok"));
    }

    @Override protected String windowTitle() { return "Staff"; }
    @Override protected String badge() { return "STAFF"; }

    @Override protected JComponent buildNav() {
        navOrders = navButton("Orders"); navMenu = navButton("Menu & Stock");
        JButton out = ui.button("Sign out", UiFactory.Kind.ACCENT);
        navOrders.addActionListener(e -> showTab("orders")); navMenu.addActionListener(e -> showTab("menu"));
        out.addActionListener(e -> { signedIn = false; screens.show(root, "login"); });
        return navPanel(navOrders, navMenu, out);
    }

    @Override protected JComponent buildBody() {
        tabPanel.setOpaque(false); tabPanel.add(ordersTab(), "orders"); tabPanel.add(menuTab(), "menu");
        root.setOpaque(false); root.add(loginPanel(), "login"); root.add(tabPanel, "main");
        screens.show(root, "login");
        return root;
    }

    private void showTab(String tab) {
        tabs.show(tabPanel, tab); banner.setText(" ");
        if (tab.equals("orders")) { highlight(navOrders, navMenu); renderOrders(); }
        else { highlight(navMenu, navOrders); renderMenu(); }
    }

    // ================= login =================
    private JPanel loginPanel() {
        JTextField user = ui.textField(ui.theme().text("staff.username"), 18);
        JPasswordField pass = ui.passwordField(18);
        JLabel message = messageLabel(ui.c("color.bad"));
        JButton signIn = ui.button("Sign in", UiFactory.Kind.DARK);
        Runnable go = () -> {
            if (user.getText().trim().equals(ui.theme().text("staff.username"))
                    && new String(pass.getPassword()).equals(ui.theme().text("staff.password"))) {
                pass.setText(""); message.setText(" "); signedIn = true; screens.show(root, "main"); showTab("orders");
            } else message.setText("Incorrect username or password.");
        };
        signIn.addActionListener(e -> go.run()); pass.addActionListener(e -> go.run());

        JPanel box = ui.vbox(); box.setBorder(UiFactory.pad(30, 30, 30, 30));
        box.add(UiFactory.left(ui.heading("Staff Sign In", 30, ui.c("color.text"))));
        box.add(UiFactory.left(ui.label("Manage orders, prices and stock.", Font.PLAIN, ui.theme().baseSize(), ui.c("color.muted"))));
        box.add(Box.createVerticalStrut(14)); box.add(UiFactory.left(ui.labeled("Username", user)));
        box.add(Box.createVerticalStrut(10)); box.add(UiFactory.left(ui.labeled("Password", pass)));
        box.add(Box.createVerticalStrut(10)); box.add(UiFactory.left(message));
        box.add(Box.createVerticalStrut(6)); box.add(UiFactory.left(signIn));
        JPanel wrap = ui.transparent(new FlowLayout(FlowLayout.LEFT)); wrap.add(box);
        return wrap;
    }

    // ================= orders =================
    private JPanel ordersTab() {
        JPanel tab = ui.transparent(new BorderLayout());
        JPanel top = ui.vbox(); top.setBorder(UiFactory.pad(14, 18, 4, 18));
        top.add(UiFactory.left(ui.heading(ui.theme().text("text.staffTitle"), 30, ui.c("color.text"))));
        top.add(UiFactory.left(chips));
        tab.add(top, BorderLayout.NORTH); tab.add(scrollOf(ordersList), BorderLayout.CENTER);
        return tab;
    }

    private void renderOrders() {
        ordersVersion = shop.ordersVersion();
        List<Order> all = shop.getOrders();
        chips.removeAll();
        chips.add(chip(null, "All " + all.size()));
        for (OrderStatus s : OrderStatus.values()) {
            int count = 0; for (Order o : all) if (o.getStatus() == s) count++;
            chips.add(chip(s, s.getLabel() + " " + count));
        }
        List<Order> shown = new ArrayList<>();
        for (Order o : all) if (statusFilter == null || o.getStatus() == statusFilter) shown.add(o);
        Collections.sort(shown, (a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
        ordersList.removeAll();
        if (shown.isEmpty()) ordersList.add(UiFactory.left(ui.label("No orders here.", Font.PLAIN, ui.theme().baseSize(), ui.c("color.muted"))));
        for (Order o : shown) {
            ordersList.add(UiFactory.left(new StaffOrderCard(ui, o, status -> {
                try { shop.changeStatus(o.getId(), status); renderOrders(); }
                catch (RuntimeException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
            })));
            ordersList.add(Box.createVerticalStrut(12));
        }
        ordersList.revalidate(); ordersList.repaint(); chips.revalidate(); chips.repaint();
    }

    private JButton chip(OrderStatus status, String text) {
        JButton b = ui.button(text, statusFilter == status ? UiFactory.Kind.DARK : UiFactory.Kind.GHOST);
        b.setBorder(BorderFactory.createCompoundBorder(ui.line(), UiFactory.pad(4, 12, 4, 12)));
        b.addActionListener(e -> { statusFilter = status; renderOrders(); });
        return b;
    }

    // ================= menu & stock =================
    private JPanel menuTab() {
        JPanel tab = ui.transparent(new BorderLayout());
        JPanel top = ui.vbox(); top.setBorder(UiFactory.pad(14, 18, 6, 18));
        JPanel titleRow = ui.transparent(new BorderLayout());
        titleRow.add(ui.heading("Menu & Stock", 30, ui.c("color.text")), BorderLayout.WEST);
        JButton refresh = ui.button("Refresh", UiFactory.Kind.GHOST); refresh.addActionListener(e -> renderMenu());
        titleRow.add(refresh, BorderLayout.EAST);
        top.add(UiFactory.left(titleRow));
        top.add(UiFactory.left(ui.label("Edit price and quantity in stock, then press Save. Guests see changes within a few seconds.",
            Font.PLAIN, ui.theme().baseSize(), ui.c("color.muted"))));
        banner.setBorder(UiFactory.pad(6, 0, 0, 0)); top.add(UiFactory.left(banner));
        tab.add(top, BorderLayout.NORTH); tab.add(scrollOf(menuList), BorderLayout.CENTER);
        return tab;
    }

    private void feedback(boolean ok, String text) { banner.setForeground(ui.c(ok ? "color.ok" : "color.bad")); banner.setText(text); }

    private void renderMenu() {
        menuList.removeAll();
        for (Item item : shop.getMenu()) {
            menuList.add(UiFactory.left(new ItemEditorRow(ui, shop, item, this::renderMenu, this::feedback)));
            menuList.add(Box.createVerticalStrut(8));
        }
        menuList.add(Box.createVerticalStrut(8));
        menuList.add(UiFactory.left(new AddItemForm(ui, shop, this::renderMenu, this::feedback)));
        menuList.revalidate(); menuList.repaint();
    }

    // ================= auto refresh =================
    @Override protected void refresh() {
        if (signedIn && shop.ordersVersion() != ordersVersion) renderOrders();
    }
}
