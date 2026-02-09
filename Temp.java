import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Temp {
    public static void main(String[] args) throws IOException {
        BufferedImage img = ImageIO.read(new File("Images/white_queen.png"));
        System.out.println(img.getColorModel().hasAlpha());
    }
}
