package hotel.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.util.Locale;

/**
 * Builds themed Swing parts (buttons, labels, fields...). OOP: the same factory object is shared by the
 * Customer and Staff apps, so both look identical and a color change in theme.properties reaches everything.
 */
public class UiFactory {
    public enum Kind { ACCENT, DARK, GHOST }

    private final Theme theme;

    public UiFactory(Theme theme) { this.theme = theme; }

    public Theme theme() { return theme; }
    public Color c(String key) { return theme.color(key); }

    public void installLookAndFeel() {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) { }
        Font f = theme.body(Font.PLAIN, theme.baseSize());
        for (String k : new String[]{"Label.font", "TextField.font", "PasswordField.font", "TextArea.font", "ComboBox.font",
                "OptionPane.messageFont", "OptionPane.buttonFont"}) UIManager.put(k, f);
        UIManager.put("Panel.background", c("color.background"));
        UIManager.put("OptionPane.background", c("color.background"));
        UIManager.put("OptionPane.messageForeground", c("color.text"));
    }

    // ---------- text helpers ----------
    public String money(double d) { return d == 0 ? "Free" : theme.currency() + String.format(Locale.ROOT, "%.2f", d); }
    public String hex(Color c) { return String.format("#%06x", c.getRGB() & 0xffffff); }
    public static String escape(String s) { return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }

    public static String ago(long time) {
        long m = Math.max(0, Math.round((System.currentTimeMillis() - time) / 60000.0));
        return m < 1 ? "just now" : m < 60 ? m + " min ago" : (m / 60) + " h " + (m % 60) + " min ago";
    }

    // ---------- layout helpers ----------
    public static <T extends JComponent> T left(T c) { c.setAlignmentX(Component.LEFT_ALIGNMENT); return c; }
    public static Border pad(int top, int left, int bottom, int right) { return new EmptyBorder(top, left, bottom, right); }
    public Border line() { return new LineBorder(c("color.border"), 1, true); }

    public JPanel vbox() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setOpaque(false); return p;
    }

    public JPanel transparent(LayoutManager layout) { JPanel p = new JPanel(layout); p.setOpaque(false); return p; }

    // ---------- components ----------
    public JLabel label(String text, int style, float size, Color color) {
        JLabel l = new JLabel(text); l.setFont(theme.body(style, size)); l.setForeground(color); return l;
    }

    public JLabel heading(String text, float size, Color color) {
        JLabel l = new JLabel(text); l.setFont(theme.heading(size)); l.setForeground(color); return l;
    }

    public JTextField textField(String text, int cols) {
        JTextField f = new JTextField(text, cols);
        f.setBackground(c("color.surface")); f.setForeground(c("color.text")); f.setCaretColor(c("color.text"));
        f.setBorder(BorderFactory.createCompoundBorder(line(), pad(6, 8, 6, 8)));
        return f;
    }

    public JPasswordField passwordField(int cols) {
        JPasswordField f = new JPasswordField("", cols);
        f.setBackground(c("color.surface")); f.setForeground(c("color.text"));
        f.setBorder(BorderFactory.createCompoundBorder(line(), pad(6, 8, 6, 8)));
        return f;
    }

    public JButton button(String text, Kind kind) {
        JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());
        b.setOpaque(true); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(theme.body(Font.BOLD, theme.baseSize() - 1));
        switch (kind) {
            case ACCENT: b.setBackground(c("color.accent")); b.setForeground(c("color.accentText")); b.setBorder(pad(8, 16, 8, 16)); break;
            case DARK: b.setBackground(c("color.header")); b.setForeground(c("color.headerText")); b.setBorder(pad(8, 16, 8, 16)); break;
            default:
                b.setBackground(c("color.surface")); b.setForeground(c("color.text"));
                b.setBorder(BorderFactory.createCompoundBorder(line(), pad(7, 15, 7, 15)));
        }
        return b;
    }

    public JPanel labeled(String caption, JComponent component) {
        JPanel p = vbox();
        p.add(left(label(caption.toUpperCase(), Font.BOLD, 10, c("color.muted"))));
        p.add(Box.createVerticalStrut(3));
        p.add(left(component));
        return p;
    }

    public JPanel card() {
        JPanel p = new JPanel(); p.setBackground(c("color.surface"));
        p.setBorder(BorderFactory.createCompoundBorder(line(), pad(12, 14, 12, 14)));
        return p;
    }
}
