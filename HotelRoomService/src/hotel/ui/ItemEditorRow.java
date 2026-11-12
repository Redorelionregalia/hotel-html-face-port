package hotel.ui;

import hotel.data.ShopService;
import hotel.model.Item;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.function.BiConsumer;

/** One staff row: edit an item's price and quantity, change its photo, or remove it. */
public class ItemEditorRow extends JPanel {

    /**
     * @param onChanged  called when the whole list must be redrawn (photo changed, item removed)
     * @param feedback   shows a green (true) or red (false) message to staff
     */
    public ItemEditorRow(UiFactory ui, ShopService shop, Item item, Runnable onChanged, BiConsumer<Boolean, String> feedback) {
        super(new BorderLayout(12, 0));
        setBackground(ui.c("color.surface"));
        setBorder(BorderFactory.createCompoundBorder(ui.line(), UiFactory.pad(8, 8, 8, 10)));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        add(new ItemPicture(ui, shop.images(), item, 64, 64, false), BorderLayout.WEST);

        JPanel mid = ui.vbox(); mid.add(Box.createVerticalGlue());
        mid.add(UiFactory.left(ui.label(item.getName(), Font.BOLD, ui.theme().baseSize(), ui.c("color.text"))));
        mid.add(UiFactory.left(ui.label(item.getCategory().getLabel(), Font.PLAIN, 12, ui.c("color.muted"))));
        mid.add(Box.createVerticalGlue());
        add(mid, BorderLayout.CENTER);

        JTextField price = ui.textField(String.valueOf(item.getPrice()), 6), stock = ui.textField(String.valueOf(item.getStock()), 4);
        JButton save = ui.button("Save", UiFactory.Kind.DARK), photo = ui.button("Photo...", UiFactory.Kind.GHOST),
                remove = ui.button("Remove", UiFactory.Kind.GHOST);
        remove.setForeground(ui.c("color.bad"));

        save.addActionListener(e -> {
            try {
                double p = Double.parseDouble(price.getText().trim()); int s = Integer.parseInt(stock.getText().trim());
                shop.editItem(item.getId(), p, s);
                feedback.accept(true, item.getName() + " saved: " + ui.money(p) + ", " + s + " in stock.");
            } catch (NumberFormatException ex) { feedback.accept(false, "Enter a valid price and quantity.");
            } catch (RuntimeException ex) { feedback.accept(false, ex.getMessage()); }
        });
        photo.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Pictures (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
            if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
            File chosen = fc.getSelectedFile();
            try {
                shop.setItemImage(item.getId(), shop.images().importImage(chosen, item.getName()));
                ItemPicture.clearCache(); onChanged.run(); feedback.accept(true, item.getName() + " photo updated.");
            } catch (RuntimeException ex) { feedback.accept(false, ex.getMessage()); }
        });
        remove.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Remove " + item.getName() + " from the menu?", "Remove item", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try { shop.removeItem(item.getId()); onChanged.run(); feedback.accept(true, item.getName() + " removed."); }
            catch (RuntimeException ex) { feedback.accept(false, ex.getMessage()); }
        });

        JPanel ctl = ui.transparent(new FlowLayout(FlowLayout.RIGHT, 6, 12));
        ctl.add(ui.label(ui.theme().currency(), Font.BOLD, ui.theme().baseSize(), ui.c("color.text"))); ctl.add(price);
        ctl.add(ui.label("Qty", Font.BOLD, ui.theme().baseSize(), ui.c("color.text"))); ctl.add(stock);
        ctl.add(save); ctl.add(photo); ctl.add(remove);
        add(ctl, BorderLayout.EAST);
    }
}
