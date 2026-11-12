package hotel.ui;

import hotel.model.Order;
import hotel.model.OrderLine;
import hotel.model.OrderStatus;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * Shows one order. This is the customer's version (with a progress bar).
 * OOP: POLYMORPHISM - StaffOrderCard extends this class and OVERRIDES showsProgress() and footerText()
 * to look different, while the rest of the layout is reused.
 */
public class OrderCard extends JPanel {
    protected final UiFactory ui;
    protected final Order order;

    public OrderCard(UiFactory ui, Order order) {
        this.ui = ui; this.order = order;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(ui.c("color.surface"));
        setBorder(BorderFactory.createCompoundBorder(ui.line(), UiFactory.pad(12, 14, 12, 14)));
        int size = ui.theme().baseSize();

        JPanel top = ui.transparent(new BorderLayout());
        top.add(ui.heading("Room " + order.getRoom(), 22, ui.c("color.text")), BorderLayout.WEST);
        OrderStatus st = order.getStatus();
        Color sc = st == OrderStatus.COMPLETED ? ui.c("color.ok") : st == OrderStatus.REJECTED ? ui.c("color.bad")
            : st == OrderStatus.PENDING ? ui.c("color.warn") : ui.c("color.text");
        JLabel pill = ui.label(" " + st.getLabel().toUpperCase() + " ", Font.BOLD, 11, sc);
        pill.setOpaque(true); pill.setBackground(ui.c("color.surface2")); top.add(pill, BorderLayout.EAST);
        add(UiFactory.left(top)); add(Box.createVerticalStrut(6));

        for (OrderLine l : order.getLines()) {
            JPanel row = ui.transparent(new BorderLayout(16, 0));
            row.add(ui.label(l.getQuantity() + " \u00D7 " + l.getName() + "   @ " + ui.money(l.getUnitPrice()), Font.PLAIN, size - 1, ui.c("color.text")), BorderLayout.WEST);
            row.add(ui.label(ui.money(l.getTotal()), Font.PLAIN, size - 1, ui.c("color.text")), BorderLayout.EAST);
            add(UiFactory.left(row));
        }
        JPanel total = ui.transparent(new BorderLayout()); total.setBorder(UiFactory.pad(6, 0, 0, 0));
        total.add(ui.label("Total", Font.BOLD, size, ui.c("color.text")), BorderLayout.WEST);
        total.add(ui.label(ui.money(order.getTotal()), Font.BOLD, size, ui.c("color.text")), BorderLayout.EAST);
        add(UiFactory.left(total));
        if (!order.getNotes().isEmpty()) add(UiFactory.left(ui.label("Note: " + order.getNotes(), Font.PLAIN, 12, ui.c("color.muted"))));

        if (showsProgress() && st != OrderStatus.REJECTED) {
            JPanel steps = ui.transparent(new GridLayout(1, 4, 4, 0)); steps.setBorder(UiFactory.pad(10, 0, 0, 0));
            OrderStatus[] all = OrderStatus.values();
            for (int i = 0; i < 4; i++) {
                boolean done = i <= st.step();
                JLabel s = ui.label(all[i].getLabel(), Font.PLAIN, 11, done ? ui.c("color.text") : ui.c("color.muted"));
                s.setBorder(new MatteBorder(3, 0, 0, 0, done ? ui.c("color.accent") : ui.c("color.border")));
                steps.add(s);
            }
            add(UiFactory.left(steps));
        }
        JLabel foot = ui.label(footerText(), Font.PLAIN, 12, ui.c("color.muted")); foot.setBorder(UiFactory.pad(8, 0, 4, 0));
        add(UiFactory.left(foot));
    }

    /** Customers see the progress bar. Subclasses may change this. */
    protected boolean showsProgress() { return true; }

    protected String footerText() { return "Order #" + order.getId() + " \u00B7 updated " + UiFactory.ago(order.getUpdatedAt()); }
}
