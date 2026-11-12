package hotel.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.util.function.IntConsumer;

/** A reusable "[-] 2 [+]" quantity control. OOP: INHERITANCE (is a JPanel) and a reusable component. */
public class Stepper extends JPanel {
    private int value;
    private final int min, max;
    private final JLabel number;
    private final IntConsumer onChange;

    public Stepper(UiFactory ui, int value, int min, int max, IntConsumer onChange) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.value = value; this.min = min; this.max = Math.max(min, max); this.onChange = onChange;
        setBackground(ui.c("color.surface")); setBorder(ui.line());
        number = new JLabel(String.valueOf(value), SwingConstants.CENTER);
        number.setFont(ui.theme().body(Font.BOLD, ui.theme().baseSize())); number.setForeground(ui.c("color.text"));
        number.setPreferredSize(new Dimension(30, 30));
        JButton minus = small(ui, "\u2212"), plus = small(ui, "+");
        minus.addActionListener(e -> change(-1)); plus.addActionListener(e -> change(1));
        add(minus); add(number); add(plus);
    }

    private JButton small(UiFactory ui, String text) {
        JButton b = new JButton(text); b.setUI(new BasicButtonUI()); b.setOpaque(true); b.setFocusPainted(false);
        b.setBackground(ui.c("color.surface")); b.setForeground(ui.c("color.text")); b.setBorder(null);
        b.setPreferredSize(new Dimension(28, 30)); b.setFont(ui.theme().body(Font.PLAIN, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void change(int d) {
        int n = value + d;
        if (n < min || n > max) return;
        value = n; number.setText(String.valueOf(value));
        if (onChange != null) onChange.accept(value);
    }

    public int getValue() { return value; }
}
