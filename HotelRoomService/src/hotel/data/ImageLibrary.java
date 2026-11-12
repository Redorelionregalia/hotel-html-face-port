package hotel.data;

import hotel.model.Item;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/** Looks after the images folder: finds an item's picture and copies new pictures in. */
public class ImageLibrary {
    private final File dir;

    public ImageLibrary(File dir) {
        this.dir = dir; dir.mkdirs();
        File readme = new File(dir, "README.txt");
        try {
            if (!readme.exists()) Files.write(readme.toPath(), ("Put item pictures here (png, jpg, gif).\r\n"
                + "Name a picture after the item with dashes, e.g. club-sandwich.png or bottled-water.jpg,\r\n"
                + "or upload one from the Staff app (Menu & Stock > Photo).\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) { }
    }

    /** The item's picture file, or null if it has none (then the emoji is shown). */
    public File find(Item item) {
        if (!item.getImageName().isEmpty()) {
            File f = new File(dir, item.getImageName());
            if (f.isFile()) return f;
        }
        for (String ext : new String[]{".png", ".jpg", ".jpeg", ".gif"}) {
            File f = new File(dir, slug(item.getName()) + ext);
            if (f.isFile()) return f;
        }
        return null;
    }

    /** Copies a chosen picture into the images folder and returns the stored file name. */
    public String importImage(File source, String itemName) {
        try {
            String n = source.getName();
            String ext = n.contains(".") ? n.substring(n.lastIndexOf('.')).toLowerCase() : ".png";
            File dest = new File(dir, slug(itemName) + ext);
            if (!dest.getCanonicalPath().equals(source.getCanonicalPath()))
                Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return dest.getName();
        } catch (Exception e) { throw new ShopException("Could not copy that image: " + e.getMessage(), e); }
    }

    private static String slug(String s) { return s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""); }
}
