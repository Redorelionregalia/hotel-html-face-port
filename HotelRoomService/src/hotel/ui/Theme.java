package hotel.ui;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Reads config/theme.properties (colors, fonts, texts). OOP: the object HIDES how settings are stored;
 * the rest of the program just asks for color("color.accent") or text("hotel.name").
 */
public class Theme {
    private static final String[][] DEFAULTS = {
        {"hotel.name", "The Grand Aurelia"}, {"hotel.tagline", "IN-ROOM DINING & SERVICES"}, {"currency", "\u20B1"},
        {"text.menuTitle", "In-Room Dining & Services"},
        {"text.menuSubtitle", "Select your items, review the cart, and we will bring everything to your door."},
        {"text.trackTitle", "Track Your Order"}, {"text.staffTitle", "Staff Dashboard"},
        {"color.background", "#F6F1E7"}, {"color.surface", "#FFFDF8"}, {"color.surface2", "#EFE8D8"},
        {"color.text", "#1C2A3A"}, {"color.muted", "#6C6A63"}, {"color.border", "#DED5C0"},
        {"color.header", "#14243A"}, {"color.headerText", "#F3EAD4"},
        {"color.accent", "#B08A3E"}, {"color.accentText", "#1C1405"}, {"color.tile", "#F1E8D2"},
        {"color.ok", "#2D7A4D"}, {"color.bad", "#B3261E"}, {"color.warn", "#9A6700"},
        {"font.heading", "Georgia"}, {"font.body", "Segoe UI"}, {"font.size", "14"},
        {"staff.username", "staff"}, {"staff.password", "staff123"}};

    private final Properties props = new Properties();

    public Theme(File file) {
        try {
            if (!file.exists()) {
                StringBuilder sb = new StringBuilder("# Edit the values, save, then restart the apps.\n");
                for (String[] d : DEFAULTS) sb.append(d[0]).append('=').append(d[1]).append('\n');
                Files.write(file.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
            }
            try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) { props.load(r); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public String text(String key) {
        String v = props.getProperty(key);
        if (v != null && !v.trim().isEmpty()) return v.trim();
        for (String[] d : DEFAULTS) if (d[0].equals(key)) return d[1];
        return "";
    }

    public Color color(String key) {
        try { return Color.decode(text(key)); } catch (Exception e) { return Color.GRAY; }
    }

    public int baseSize() { try { return Integer.parseInt(text("font.size")); } catch (Exception e) { return 14; } }
    public String currency() { return text("currency"); }
    public Font body(int style, float size) { return new Font(text("font.body"), style, Math.round(size)); }
    public Font heading(float size) { return new Font(text("font.heading"), Font.BOLD, Math.round(size)); }
}
