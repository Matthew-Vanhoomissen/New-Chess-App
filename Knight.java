import java.util.ArrayList;

public class Knight extends Piece{
    String type;

    public Knight(String color) {
        super(color);
        this.type = "knight";
    }

    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();
        int[][] offsets = {
            {-2, -1}, {-2,  1},
            {-1, -2}, {-1,  2},
            { 1, -2}, { 1,  2},
            { 2, -1}, { 2,  1}
        };

        for (int[] offset : offsets) {
            int newRow = from.row + offset[0];
            int newCol = from.col + offset[1];

            if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8) {
                continue;
            }

            Piece piece = board.pieceThere(newRow, newCol);

            if (piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(newRow, newCol)));
            }
            else if (!piece.color.equals(this.color)) {
                pseudoMoves.add(new Move(this, from, new Position(newRow, newCol)));
            }
        }
        return pseudoMoves;
    }
}
