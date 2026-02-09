import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Board board = new Board();
        board.createBoard();

        JFrame frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ChessPanel panel = new ChessPanel(board);
        frame.add(panel);

        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
}