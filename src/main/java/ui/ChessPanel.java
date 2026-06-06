package ui;

/**
 * Renders board screen and all user inputs. Draws board, pieces, possible 
 * moves, and end of game button so reset
 * 
 * @author Matthew-Vanhoomissen
 */

import javax.swing.*;
import game.*;
import pieces.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.ArrayList;

public class ChessPanel extends JPanel {

    private Board board;
    private GameManager manager;
    private ArrayList<Move> highlightedMoves = new ArrayList<>(); //Legal moves for selected piece
    private final int TILE_SIZE;
    private JButton resetBtn;

    public ChessPanel(Board board, int tileSize) {
        this.board = board;
        this.TILE_SIZE = tileSize;
        setLayout(null); // absolute positioning for reset button
        setPreferredSize(new Dimension(8 * TILE_SIZE, 8 * TILE_SIZE));

        // Reset button — hidden until game over
        resetBtn = new JButton("New Game");
        resetBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        resetBtn.setBackground(new Color(80, 140, 80));
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setFocusPainted(false);
        resetBtn.setBorderPainted(false);
        resetBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resetBtn.setBounds(
            (8 * TILE_SIZE / 2) - 80,
            (8 * TILE_SIZE / 2) - 20,
            160, 40
        );
        resetBtn.setVisible(false);
        resetBtn.addActionListener(e -> {
            // Close and reopen start window
            SwingUtilities.getWindowAncestor(this).dispose();
            new StartWindow();
        });
        add(resetBtn);

        //Checks for user input through clicking on coordinates
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (resetBtn.isVisible()) return; // ignore clicks when game over
                Position pos = pixelConverter(e.getX(), e.getY());
                if (pos != null) manager.handleClick(pos);
            }
        });
    }

    /**
     * Sets the gameManager so they can communicate
     * 
     * @param manager
     */
    public void setManager(GameManager manager) {
        this.manager = manager;
    }

    /**
     * Called by {@link game.GameManager#endTurn()} when the game is completed.
     * Generates reset screen to play again and displays end reult
     * 
     * @param message result to be displayed
     */
    public void showGameOver(String message) {
        SwingUtilities.invokeLater(() -> {
            resetBtn.setText(message + " — New Game");
            resetBtn.setVisible(true);
            repaint();
        });
    }

    /**
     * Sets the highlighted moves to the possible legal moves for selected piece
     * 
     * @param moves
     */
    public void setHighlightedMoves(ArrayList<Move> moves) {
        this.highlightedMoves = moves;
        repaint();
    }

    /**
     * Clears highlighted moves when there is an invalid selection or
     * switching sides
     * 
     */
    public void clearHighlightedMoves() {
        this.highlightedMoves.clear();
        repaint();
    }

    /**
     * Paints the different layers onto the screen and can generate the end game
     * screen if necessary
     * 
     * @param g graphics object
     * 
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawMoves(g);
        drawPieces(g);

        // Dim overlay when game over
        if (resetBtn.isVisible()) {
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * Generates black and white checkerboard pattern as used in chess. Allows
     * for customizable screen and tile size.
     * 
     * @param g
     */
    private void drawBoard(Graphics g) {
        boolean light = true;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                g.setColor(light ? Color.WHITE : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                light = !light;
            }
            light = !light;
        }
    }

    /**
     * Draws all game pieces on top of the board in their correct positions.
     * Will flip the board if player is black for better gameplay.
     * 
     * @param g
     */
    private void drawPieces(Graphics g) {
        //Flip board if player is black
        boolean boardFlipped = manager.getPlayerColor().equals("black");
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int flippedRow = boardFlipped ? 7 - row : row;
                Piece piece = board.pieceThere(flippedRow, col);
                if (piece != null) {
                    String key = piece.color + "_" + piece.type;
                    g.drawImage(ImageStorage.get(key),
                        col * TILE_SIZE, row * TILE_SIZE,
                        TILE_SIZE, TILE_SIZE, null);
                }
            }
        }
    }

    /**
     * Draws possible legal moves over the board and under pieces. Correctly 
     * coordinates with drawing pieces to flip board when player is black.
     * 
     * @param g
     */
    private void drawMoves(Graphics g) {
        //Flip board if player is black
        boolean boardFlipped = manager.getPlayerColor().equals("black");
        for (Move m : highlightedMoves) {
            g.setColor(Color.GREEN);
            g.fillRect(m.end.col * TILE_SIZE, ( boardFlipped ? 7 - m.end.row : m.end.row) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    /**
     * Converts the x and y pixel coordinates to whole indices that can be used 
     * in the board 2D array. Correctly coordinates with drawing pieces and moves 
     * to flip board when player is black.
     * 
     * @param x axis
     * @param y axis
     * @return new position with correct row and column equivalent
     */
    private Position pixelConverter(int x, int y) {
        boolean flipped = manager.getPlayerColor().equals("black");
        int row = y / TILE_SIZE;
        int col = x / TILE_SIZE;
        if (row >= 0 && row <= 7 && col >= 0 && col <= 7) {
            int boardRow = (flipped ? 7 - row : row);
            return new Position(boardRow, col);
        }
        return null;
    }
}