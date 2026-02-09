import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.awt.Dimension;

public class ChessPanel extends JPanel {

    private Board board;
    private static final int TILE_SIZE = 64;

    public ChessPanel(Board board) {
        this.board = board;
        setPreferredSize(new Dimension(8 * TILE_SIZE, 8 * TILE_SIZE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawPieces(g);
        
    }

    private void drawBoard(Graphics g) {
        boolean light = true;

        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                g.setColor(light ? Color.WHITE : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                light = !light;
            }
            light = !light;
        }
    }

    private void drawPieces(Graphics g) {
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                Piece piece = board.pieceThere(row, col);
                if(piece != null) {
                    String key = piece.color + "_" + piece.type;
                    System.out.println(piece.color);
                    System.out.println(piece.type);
                    if(ImageStorage.get(key) == null) {
                        System.out.println("Didn't get image");
                    }
                    g.drawImage(
                        ImageStorage.get(key),
                        col * TILE_SIZE,
                        row * TILE_SIZE,
                        TILE_SIZE,
                        TILE_SIZE,
                        null
                    );
                }
            }
        }
    }
}