import java.awt.Image;
import java.util.HashMap;
import javax.swing.ImageIcon;

public class ImageStorage {

    private static final HashMap<String, Image> images = new HashMap<>();

    static {
        load("white_pawn");
        load("white_rook");
        load("white_knight");
        load("white_bishop");
        load("white_queen");
        load("white_king");

        load("black_pawn");
        load("black_rook");
        load("black_knight");
        load("black_bishop");
        load("black_queen");
        load("black_king");
    }

    private static void load(String key) {
        Image img = new ImageIcon("images/" + key + ".png").getImage();
        images.put(key, img);
    }

    public static Image get(String key) {
        return images.get(key);
    }
}