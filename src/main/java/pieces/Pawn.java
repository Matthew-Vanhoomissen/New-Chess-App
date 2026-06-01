package pieces;
import java.util.ArrayList;

import game.*;


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
        boolean onPromotionRank = 
            (color.equals("white") && from.row == 1) || 
            (color.equals("black") && from.row == 6);

        //Moving forward or up 2
        Piece piece1 = board.pieceThere(from.row + direction, from.col);
        Piece piece2 = board.pieceThere(from.row + 2 * direction, from.col);
        
        if(piece1 == null && from.row + direction >= 0 && from.row + direction <= 7) {
            Move newMove = new Move(this, from, new Position(from.row + direction, from.col), null);
            if(onPromotionRank) {
                newMove.promotionType = "queen";
            }
            pseudoMoves.add(newMove);
            if(onStartingRank && piece2 == null) {
                pseudoMoves.add(new Move(this, from, new Position(from.row + 2 * direction, from.col), null));
            }
            
        }
        //Capturing diagonally
        piece1 = board.pieceThere(from.row + direction, from.col - 1);
        piece2 = board.pieceThere(from.row + direction, from.col + 1);

        if(piece1 != null || piece2 != null) {
            if(piece1 != null && !piece1.color.equals(this.color)) {
                pseudoMoves.add(new Move(this, from, new Position(from.row + direction, from.col - 1), piece1));
            }
            if(piece2 != null && !piece2.color.equals(this.color)) {
                pseudoMoves.add(new Move(this, from, new Position(from.row + direction, from.col + 1), piece2));
            }

        }

        //En Passant
        Move prevMove = board.getPreviousMove();
        if(prevMove != null && prevMove.piece instanceof Pawn) { //Previous move was from pawn
            if(Math.abs(prevMove.end.row - prevMove.start.row) == 2 && prevMove.end.row == from.row) {//On same row
                if(Math.abs(prevMove.end.col - from.col) == 1) { //Offset column by 1
                    pseudoMoves.add(new Move(this, from, new Position(
                        from.row + direction,
                        prevMove.end.col),
                        prevMove.piece, prevMove.end));
                }
            }
        }

        return pseudoMoves;
    }
}
