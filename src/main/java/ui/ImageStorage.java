package ui;

/**
 * Stores path to image into HashMap given piece color and type for fast
 * lookup
 * 
 * @author Matthew-Vanhoomissen
 */

import java.awt.Image;
import java.util.HashMap;
import javax.swing.ImageIcon;

public class ImageStorage {

    private static final HashMap<String, Image> images = new HashMap<>();

    //Load all images into memory
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

    /**
     * Loads each image once into memory which can be utilized 
     * any number of times.
     * 
     * @param key
     */
    private static void load(String key) {
        Image img = new ImageIcon(
            ImageStorage.class.getResource("/images/" + key + ".png")
        ).getImage();
        images.put(key, img);
    }

    /**
     * Gets the image object and is used by
     * {@link ui.ChessPanel#drawPieces()}
     * 
     * @param key
     * @return image
     */
    public static Image get(String key) {
        return images.get(key);
    }
}