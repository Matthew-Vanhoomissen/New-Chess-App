import javax.swing.JPanel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Dimension;

public class ChessPanel extends JPanel {

    private Board board;
    private GameManager manager;
    private ArrayList<Move> highlightedMoves = new ArrayList<>();
    private static final int TILE_SIZE = 64;
    

    public ChessPanel(Board board) {
        this.board = board;
        setPreferredSize(new Dimension(8 * TILE_SIZE, 8 * TILE_SIZE));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Position pos = pixelConverter(e.getX(), e.getY());
                if(pos != null) {
                    manager.handleClick(pos);
                }
            }
        });
    }
    public void setManager(GameManager manager) {
        this.manager = manager;
    }

    public void setHighlightedMoves(ArrayList<Move> moves) {
        this.highlightedMoves = moves;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawMoves(g);
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

    private void drawMoves(Graphics g) {
        for(Move m : highlightedMoves) {
            int row = m.end.row;
            int col = m.end.col;
            g.setColor(new Color(0, 255, 0, 120));
            g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private Position pixelConverter(int x, int y) {
        int row = y / TILE_SIZE;
        int col = x / TILE_SIZE;

        if((row <= 7 && row >= 0) && (col <= 7 && col >=0)) {
            return new Position(row, col);
        }
        return null;
    }
}