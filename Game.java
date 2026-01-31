import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class Game extends JPanel {

    public Game() {
        setPreferredSize(new Dimension(512, 512));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean white = true;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                g.setColor(white ? Color.WHITE : Color.DARK_GRAY);
                g.fillRect(col * 64, row * 64, 64, 64);
                white = !white;
            }
            white = !white;
        }
    }
}