import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        /* 
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chess Board");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            Game gamePanel = new Game();
            frame.add(gamePanel);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
        */
       Board board = new Board();
       board.createBoard();
       Piece piece = board.pieceThere(0, 4);
       if(piece instanceof King) {
        System.out.println("Hello");
       }
       ArrayList<Move> moves = piece.getPseudoLegalMoves(board, new Position(0, 4));
       for(Move m : moves) {
        System.out.println(m);
       }
    }
}