package ui;

/**
 * Creates starting window for the application. Allows team switching and board 
 * resizing. Initializes game on successful player input
 * 
 * @author Matthew-Vanhoomissen
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import game.*;

public class StartWindow extends JFrame {

    private String playerColor = "white";
    private int tileSize = 64;
    private JLabel colorLabel;
    private JLabel sizeLabel;

    public StartWindow() {
        setTitle("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));

        // Title
        JLabel title = new JLabel("CHESS");
        title.setFont(new Font("Serif", Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Color toggle
        colorLabel = new JLabel("Play as: WHITE");
        colorLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        colorLabel.setForeground(new Color(200, 200, 200));
        colorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton colorBtn = makeButton("Swap Color");
        colorBtn.addActionListener(e -> {
            playerColor = playerColor.equals("white") ? "black" : "white";
            colorLabel.setText("Play as: " + playerColor.toUpperCase());
        });

        // Size toggle
        sizeLabel = new JLabel("Board size: 64px");
        sizeLabel.setFont(new Font("Monospaced", Font.PLAIN, 16));
        sizeLabel.setForeground(new Color(200, 200, 200));
        sizeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton sizeBtn = makeButton("Swap Board Size");
        sizeBtn.addActionListener(e -> {
            tileSize = (tileSize == 64) ? 80 : 64;
            sizeLabel.setText("Board size: " + tileSize + "px");
        });

        // Start button
        JButton startBtn = makeButton("Start Game");
        startBtn.setBackground(new Color(80, 140, 80));
        startBtn.setForeground(Color.WHITE);
        startBtn.addActionListener(e -> {
            launchGame();
        });

        panel.add(title);
        panel.add(Box.createVerticalStrut(30));
        panel.add(colorLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(colorBtn);
        panel.add(Box.createVerticalStrut(20));
        panel.add(sizeLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(sizeBtn);
        panel.add(Box.createVerticalStrut(30));
        panel.add(startBtn);

        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void launchGame() {
        dispose(); // close start window

        Board board = new Board();
        board.createBoard();

        JFrame gameFrame = new JFrame("Chess");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setResizable(false);

        ChessPanel panel = new ChessPanel(board, tileSize);
        GameManager manager = new GameManager(board, panel, playerColor);
        panel.setManager(manager);

        gameFrame.add(panel);
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);

    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Monospaced", Font.BOLD, 14));
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}