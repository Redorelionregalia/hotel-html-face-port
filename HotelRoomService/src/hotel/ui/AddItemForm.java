package hotel.ui;

import hotel.data.ShopService;
import hotel.model.Category;
import hotel.model.Item;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.function.BiConsumer;

/** Staff form for adding a new menu item (with an optional photo). */
public class AddItemForm extends JPanel {
    private File photoFile;

    public AddItemForm(UiFactory ui, ShopService shop, Runnable onChanged, BiConsumer<Boolean, String> feedback) {
        super(new BorderLayout(0, 8));
        setBackground(ui.c("color.surface"));
        setBorder(BorderFactory.createCompoundBorder(ui.line(), UiFactory.pad(12, 14, 12, 14)));
        add(ui.heading("Add a new item", 22, ui.c("color.text")), BorderLayout.NORTH);

        JTextField name = ui.textField("", 10), desc = ui.textField("", 10), price = ui.textField("", 6),
                   qty = ui.textField("", 4), emoji = ui.textField("", 4);
        JComboBox<Category> category = new JComboBox<>(Category.values());
        category.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, ((Category) v).getLabel(), i, s, f);
            }
        });
        JButton photo = ui.button("Choose photo...", UiFactory.Kind.GHOST), add = ui.button("Add item", UiFactory.Kind.ACCENT);
        photo.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("Pictures (png, jpg, gif)", "png", "jpg", "jpeg", "gif"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { photoFile = fc.getSelectedFile(); photo.setText(photoFile.getName()); }
        });
        add.addActionListener(e -> {
            try {
                String n = name.getText().trim();
                if (n.isEmpty()) throw new NumberFormatException();
                Item item = new Item(n, (Category) category.getSelectedItem(), desc.getText().trim(),
                    Double.parseDouble(price.getText().trim()), Integer.parseInt(qty.getText().trim()), emoji.getText().trim());
                if (photoFile != null) item.setImageName(shop.images().importImage(photoFile, n));
                shop.addItem(item); photoFile = null; onChanged.run(); feedback.accept(true, n + " added.");
            } catch (NumberFormatException ex) { feedback.accept(false, "Name, price and quantity are required.");
            } catch (RuntimeException ex) { feedback.accept(false, ex.getMessage()); }
        });

        JPanel grid = ui.transparent(new GridLayout(2, 4, 10, 8));
        grid.add(ui.labeled("Name", name)); grid.add(ui.labeled("Category", category)); grid.add(ui.labeled("Description", desc));
        grid.add(ui.labeled("Price", price)); grid.add(ui.labeled("Quantity", qty)); grid.add(ui.labeled("Emoji (optional)", emoji));
        grid.add(ui.labeled("Photo (optional)", photo)); grid.add(ui.labeled(" ", add));
        add(grid, BorderLayout.CENTER);
    }
}
