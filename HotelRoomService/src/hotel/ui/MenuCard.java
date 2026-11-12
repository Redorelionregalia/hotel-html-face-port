package hotel.ui;

import hotel.data.ImageLibrary;
import hotel.model.Item;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

/** One item on the guest menu: picture, price per item, stock, quantity stepper and "Add" button. */
public class MenuCard extends JPanel {

    /**
     * @param inCart      how many of this item are already in the cart
     * @param selected    quantity currently chosen on the stepper
     * @param onQuantity  called when the guest changes the stepper
     * @param onAdd       called with the quantity when the guest presses Add
     */
    public MenuCard(UiFactory ui, ImageLibrary images, Item item, int inCart, int selected, IntConsumer onQuantity, IntConsumer onAdd) {
        super(new BorderLayout());
        setBackground(ui.c("color.surface")); setBorder(ui.line());
        add(new ItemPicture(ui, images, item, 230, 130, true), BorderLayout.NORTH);
        int size = ui.theme().baseSize();

        JPanel body = ui.vbox(); body.setBorder(UiFactory.pad(10, 12, 12, 12));
        body.add(UiFactory.left(ui.label(item.getCategory().getLabel().toUpperCase(), Font.BOLD, 10, ui.c("color.accent"))));
        body.add(UiFactory.left(ui.heading(item.getName(), 20, ui.c("color.text"))));
        body.add(UiFactory.left(ui.label("<html><div style='width:185px'>" + UiFactory.escape(item.getDescription()) + "</div></html>",
            Font.PLAIN, size - 1, ui.c("color.muted"))));
        body.add(Box.createVerticalStrut(4));
        String per = item.getPrice() > 0 ? " <span style='color:" + ui.hex(ui.c("color.muted")) + "'>per item</span>" : "";
        body.add(UiFactory.left(ui.label("<html><b>" + ui.money(item.getPrice()) + "</b>" + per + "</html>", Font.PLAIN, size, ui.c("color.text"))));
        Color sc = !item.isInStock() ? ui.c("color.bad") : item.isLowStock() ? ui.c("color.warn") : ui.c("color.muted");
        body.add(UiFactory.left(ui.label((item.isInStock() ? item.getStock() + " available" : "Out of stock")
            + (inCart > 0 ? " \u00B7 " + inCart + " in cart" : ""), Font.PLAIN, 12, sc)));
        body.add(Box.createVerticalStrut(6));

        int n = Math.min(selected, Math.max(1, item.getStock()));
        JButton add = ui.button("Add \u00B7 " + ui.money(item.getPrice() * n), UiFactory.Kind.ACCENT);
        add.setEnabled(item.isInStock());
        Stepper stepper = new Stepper(ui, n, 1, Math.max(1, item.getStock()), q -> {
            add.setText("Add \u00B7 " + ui.money(item.getPrice() * q)); onQuantity.accept(q);
        });
        add.addActionListener(e -> onAdd.accept(stepper.getValue()));
        JPanel buy = ui.transparent(new BorderLayout(8, 0));
        buy.add(stepper, BorderLayout.WEST); buy.add(add, BorderLayout.CENTER);
        body.add(UiFactory.left(buy));
        add(body, BorderLayout.CENTER);
    }

    @Override public Dimension getPreferredSize() { return new Dimension(230, super.getPreferredSize().height); }
}
