import java.util.ArrayList;

public class Rook extends Piece{
    String type;

    public Rook(String color) {
        super(color, "rook");
    }

    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();

        int[][] directions = {
            {-1, 0}, {1, 0},
            {0, -1}, {0, 1}
        };

        for(int[] coord : directions) {
            int r = from.row + coord[0];
            int c = from.col + coord[1];

            while(r < 8 && r >= 0 && c < 8 && c >= 0) {
                Piece piece = board.pieceThere(r, c);
                if (piece == null) {
                    pseudoMoves.add(new Move(this, from, new Position(r, c), null));
                } else {
                    if (!piece.color.equals(this.color)) {
                        pseudoMoves.add(new Move(this, from, new Position(r, c), piece));
                    }
                    break;
                }
                r += 1 * coord[0];
                c += 1 * coord[1];
            } 
        }
        return pseudoMoves;
    }
}
