package hotel.data;

import hotel.model.Category;
import hotel.model.Item;

import java.io.File;

/** OOP: INHERITANCE - stores Items by extending FileRepository. Only the record format is written here. */
public class MenuRepository extends FileRepository<Item> {

    public MenuRepository(File file, StorageLock lock) {
        super(file, lock);
        if (!fileExists()) seed();
    }

    @Override protected Item fromFields(String[] f) {
        return new Item(Integer.parseInt(f[0]), f[1], Category.parse(f[2]), f[3], Double.parseDouble(f[4]),
            Integer.parseInt(f[5]), f[6], f[7]);
    }

    @Override protected String[] toFields(Item i) {
        return new String[]{String.valueOf(i.getId()), i.getName(), i.getCategory().name(), i.getDescription(),
            String.valueOf(i.getPrice()), String.valueOf(i.getStock()), i.getEmoji(), i.getImageName()};
    }

    /** First start: fill the menu with sample items. */
    private void seed() {
        Object[][] m = {
            {"Extra Towel", Category.AMENITY, "One additional bath towel", 0, 20, "\uD83D\uDEC1"},
            {"Extra Pillow", Category.AMENITY, "One additional pillow", 0, 15, "\uD83D\uDECF\uFE0F"},
            {"Blanket", Category.AMENITY, "One extra blanket", 0, 10, "\uD83D\uDECC"},
            {"Hanger Set", Category.AMENITY, "Set of two hangers", 0, 30, "\uD83E\uDDE5"},
            {"Iron", Category.AMENITY, "Iron for temporary use", 0, 5, "\uD83D\uDC54"},
            {"Club Sandwich", Category.FOOD, "Chicken, egg and vegetables", 220, 12, "\uD83E\uDD6A"},
            {"Pancakes", Category.FOOD, "Three pancakes with syrup", 160, 10, "\uD83E\uDD5E"},
            {"Caesar Salad", Category.FOOD, "Romaine, parmesan, croutons", 190, 8, "\uD83E\uDD57"},
            {"Bottled Water", Category.BEVERAGE, "500 ml bottled water", 40, 50, "\uD83D\uDCA7"},
            {"Orange Juice", Category.BEVERAGE, "Fresh orange juice", 90, 20, "\uD83C\uDF4A"},
            {"Brewed Coffee", Category.BEVERAGE, "Freshly brewed, served hot", 75, 25, "\u2615"},
            {"House Red Wine", Category.BEVERAGE, "Glass of house red", 260, 10, "\uD83C\uDF77"}};
        for (Object[] r : m)
            add(new Item((String) r[0], (Category) r[1], (String) r[2], ((Number) r[3]).doubleValue(), (Integer) r[4], (String) r[5]));
    }
}
