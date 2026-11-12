package hotel.app;

import hotel.data.ImageLibrary;
import hotel.data.MenuRepository;
import hotel.data.OrderRepository;
import hotel.data.ShopService;
import hotel.data.StorageLock;
import hotel.ui.Theme;
import hotel.ui.UiFactory;

import java.io.File;

/**
 * OOP: COMPOSITION ROOT. The one place where the objects are created and connected:
 * folders -> lock -> repositories -> ShopService, and Theme -> UiFactory.
 * Both apps call AppContext.create() and receive the same kind of parts.
 */
public class AppContext {
    private final ShopService shop;
    private final UiFactory ui;

    private AppContext(ShopService shop, UiFactory ui) { this.shop = shop; this.ui = ui; }

    public ShopService shop() { return shop; }
    public UiFactory ui() { return ui; }

    public static AppContext create() {
        File base = findBaseFolder();
        File data = new File(base, "data"), config = new File(base, "config");
        data.mkdirs(); config.mkdirs();

        StorageLock lock = new StorageLock(new File(data, "store.lock"));
        MenuRepository menu = new MenuRepository(new File(data, "menu.txt"), lock);
        OrderRepository orders = new OrderRepository(new File(data, "orders.txt"), lock);
        ImageLibrary images = new ImageLibrary(new File(base, "images"));
        ShopService shop = new ShopService(menu, orders, lock, images);
        UiFactory ui = new UiFactory(new Theme(new File(config, "theme.properties")));
        return new AppContext(shop, ui);
    }

    /** The folder that holds the .jar / .exe (so data, images and config live next to the program). */
    private static File findBaseFolder() {
        try {
            File f = new File(AppContext.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return f.isFile() ? f.getParentFile() : new File(System.getProperty("user.dir"));
        } catch (Exception e) { return new File(System.getProperty("user.dir")); }
    }
}
