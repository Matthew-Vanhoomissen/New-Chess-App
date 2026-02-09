import java.util.ArrayList;

public class Pawn extends Piece{
    public String type;

    public Pawn(String color) {
        super(color, "pawn");
    }

    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();
        int direction = 1;
        if(this.color.equals("white")) {
            direction = -1;
        }
        boolean onStartingRank =
            (color.equals("white") && from.row == 6) ||
            (color.equals("black") && from.row == 1);

        Piece piece1 = board.pieceThere(from.row + 1 * direction, from.col);
        Piece piece2 = board.pieceThere(from.row + 2 * direction, from.col);
        
        if(piece1 == null && from.row + 1 * direction >= 0 && from.row + 1 * direction <= 7) {
            pseudoMoves.add(new Move(this, from, new Position(from.row + 1 * direction, from.col)));
            if(onStartingRank && piece2 == null) {
                pseudoMoves.add(new Move(this, from, new Position(from.row + 2 * direction, from.col)));
            }
            
        }
        piece1 = board.pieceThere(from.row + 1 * direction, from.col - 1);
        piece2 = board.pieceThere(from.row + 1 * direction, from.col + 1);

        if(piece1 != null || piece2 != null) {
            if(piece1 != null && !piece1.color.equals(this.color)) {
                pseudoMoves.add(new Move(this, from, new Position(from.row + 1 * direction, from.col - 1)));
            }
            if(piece2 != null && !piece2.color.equals(this.color)) {
                pseudoMoves.add(new Move(this, from, new Position(from.row + 1 * direction, from.col + 1)));
            }

        }

        return pseudoMoves;
    }
}
