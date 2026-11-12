package hotel.ui;

import hotel.data.ShopService;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;

/**
 * OOP: ABSTRACT CLASS + TEMPLATE METHOD + INHERITANCE.
 * Everything the Customer and Staff windows have in common (header bar, navigation buttons, scroll areas,
 * the 3-second auto refresh) is written ONCE here. launch() is the "template": it calls the abstract
 * methods that each app must fill in (windowTitle, badge, buildNav, buildBody, refresh).
 */
public abstract class BaseApp extends JFrame {
    protected final ShopService shop;
    protected final UiFactory ui;

    protected BaseApp(ShopService shop, UiFactory ui) { this.shop = shop; this.ui = ui; }

    /** Text in the window title bar, e.g. "In-Room Dining". */
    protected abstract String windowTitle();
    /** Small gold text under the hotel name, e.g. "STAFF". */
    protected abstract String badge();
    /** The buttons on the right of the header. */
    protected abstract JComponent buildNav();
    /** The main area of the window. */
    protected abstract JComponent buildBody();
    /** Called every 3 seconds so the screen follows changes made by the other app. */
    protected abstract void refresh();

    /** TEMPLATE METHOD: the same startup steps for every app. */
    public final void launch() {
        setTitle(ui.theme().text("hotel.name") + " \u2013 " + windowTitle());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 760); setMinimumSize(new Dimension(560, 520)); setLocationRelativeTo(null);
        getContentPane().setBackground(ui.c("color.background"));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        new javax.swing.Timer(3000, e -> refresh()).start();
        refresh();
        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel head = new JPanel(new BorderLayout()); head.setBackground(ui.c("color.header"));
        head.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 2, 0, ui.c("color.accent")), UiFactory.pad(10, 18, 10, 18)));
        JPanel brand = ui.vbox();
        brand.add(UiFactory.left(ui.heading(ui.theme().text("hotel.name"), 26, ui.c("color.headerText"))));
        brand.add(UiFactory.left(ui.label(badge(), Font.BOLD, 10, ui.c("color.accent"))));
        head.add(brand, BorderLayout.WEST);
        head.add(buildNav(), BorderLayout.EAST);
        return head;
    }

    // ---------- helpers shared by both apps ----------
    protected JButton navButton(String text) { return ui.button(text, UiFactory.Kind.DARK); }

    protected void highlight(JButton active, JButton... others) {
        active.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 2, 0, ui.c("color.accent")), UiFactory.pad(8, 16, 6, 16)));
        for (JButton b : others) b.setBorder(UiFactory.pad(8, 16, 8, 16));
    }

    protected JPanel navPanel(JComponent... parts) {
        JPanel nav = ui.transparent(new FlowLayout(FlowLayout.RIGHT, 4, 6));
        for (JComponent p : parts) nav.add(p);
        return nav;
    }

    /** A vertical list inside a scroll pane that follows the window width. */
    protected JScrollPane scrollOf(JPanel list) {
        JPanel holder = ui.transparent(new BorderLayout()); holder.setBorder(UiFactory.pad(0, 18, 18, 18));
        holder.add(list, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(holder, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null); sp.getViewport().setOpaque(false); sp.setOpaque(false); sp.getVerticalScrollBar().setUnitIncrement(18);
        return sp;
    }

    protected JLabel messageLabel(Color color) {
        JLabel l = new JLabel(" "); l.setForeground(color); l.setFont(ui.theme().body(Font.BOLD, ui.theme().baseSize())); return l;
    }
}
