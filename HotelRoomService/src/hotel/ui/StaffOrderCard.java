package hotel.ui;

import hotel.model.Order;
import hotel.model.OrderStatus;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * OOP: INHERITANCE + POLYMORPHISM. Same card as the customer's, but with no progress bar,
 * a different footer, and a status picker so staff can update the order.
 */
public class StaffOrderCard extends OrderCard {

    public StaffOrderCard(UiFactory ui, Order order, Consumer<OrderStatus> onUpdate) {
        super(ui, order);
        JComboBox<String> picker = new JComboBox<>();
        for (OrderStatus s : OrderStatus.values()) picker.addItem(s.getLabel());
        picker.setSelectedIndex(order.getStatus().ordinal());
        JButton update = ui.button("Update status", UiFactory.Kind.DARK);
        update.addActionListener(e -> onUpdate.accept(OrderStatus.values()[picker.getSelectedIndex()]));
        JPanel row = ui.transparent(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.add(picker); row.add(update);
        add(UiFactory.left(row));
    }

    @Override protected boolean showsProgress() { return false; }

    @Override protected String footerText() {
        return "Received " + UiFactory.ago(order.getCreatedAt()) + " \u00B7 Order #" + order.getId();
    }
}
