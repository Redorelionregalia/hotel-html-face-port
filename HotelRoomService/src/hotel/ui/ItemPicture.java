package hotel.ui;

import hotel.data.ImageLibrary;
import hotel.model.Item;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** Draws an item's photo (or its emoji when there is no photo). OOP: a custom component that paints itself. */
public class ItemPicture extends JPanel {
    private static final Map<String, BufferedImage> CACHE = new HashMap<>();
    private final UiFactory ui;
    private final ImageLibrary images;
    private final Item item;
    private final boolean fadeWhenSoldOut;

    public ItemPicture(UiFactory ui, ImageLibrary images, Item item, int w, int h, boolean fadeWhenSoldOut) {
        this.ui = ui; this.images = images; this.item = item; this.fadeWhenSoldOut = fadeWhenSoldOut;
        setPreferredSize(new Dimension(w, h)); setMinimumSize(new Dimension(w, h));
    }

    public static void clearCache() { CACHE.clear(); }

    private static BufferedImage load(File f) {
        if (f == null) return null;
        String key = f.getAbsolutePath() + f.lastModified();
        if (!CACHE.containsKey(key)) {
            BufferedImage im = null;
            try { im = ImageIO.read(f); } catch (Exception ignored) { }
            CACHE.put(key, im);
        }
        return CACHE.get(key);
    }

    @Override protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        int w = getWidth(), h = getHeight();
        g.setColor(ui.c("color.tile")); g.fillRect(0, 0, w, h);
        BufferedImage im = load(images.find(item));
        if (im != null) {
            double s = Math.max((double) w / im.getWidth(), (double) h / im.getHeight());
            int dw = (int) (im.getWidth() * s), dh = (int) (im.getHeight() * s);
            g.drawImage(im, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        } else {
            Font f = new Font("Segoe UI Emoji", Font.PLAIN, Math.min(w, h) / 2);
            String text = item.getEmoji();
            if (f.canDisplayUpTo(text) != -1) {
                f = ui.theme().heading(Math.min(w, h) / 2f);
                text = item.getName().isEmpty() ? "?" : item.getName().substring(0, 1);
            }
            g.setFont(f); g.setColor(ui.c("color.text"));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(text, (w - fm.stringWidth(text)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
        }
        if (fadeWhenSoldOut && !item.isInStock()) { g.setColor(new Color(255, 255, 255, 160)); g.fillRect(0, 0, w, h); }
        g.dispose();
    }
}
