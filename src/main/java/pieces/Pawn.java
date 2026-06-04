package pieces;

/**
 * Pawn specific movement
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.ArrayList;

import game.*;


public class Pawn extends Piece{
    public String type;

    public Pawn(String color) {
        super(color, "pawn");
    }

    /**
     * Returns all pseudo-legal moves for this pawn from the given position.
     *
     * Generates moves for singular or double movements as well as the special case
     * of en passant. If enemy piece is diagonally in front, capture diagonally.
     *
     *
     * @param board the current board state used to check occupancy
     * @param from  the square this pawn currently occupies
     * @return a list of pseudo-legal moves, empty if completely blocked
     */
    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();

        int direction = 1;
        if(this.color.equals("white")) { //Since pawns are strictly directional, see which way the 
                                                  //color is moving in relation to the array indices
            direction = -1;
        }

        boolean onPromotionRank = //Utilized for promotion logic. Aka one away from promoting
            (color.equals("white") && from.row == 1) || 
            (color.equals("black") && from.row == 6);

        //Moving forward or up 2
        Piece piece1 = board.pieceThere(from.row + direction, from.col); //Piece one in front
        Piece piece2 = board.pieceThere(from.row + 2 * direction, from.col); //Piece two in front
        
        if(piece1 == null && from.row + direction >= 0 && from.row + direction <= 7) { //If no piece is in front and in bounds
            Move newMove = new Move(this, from, new Position(from.row + direction, from.col), null);
            if(onPromotionRank) { //If promoting, auto-promote to a queen which is processed in board
                newMove.promotionType = "queen"; 
            }
            pseudoMoves.add(newMove);
            if(!this.hasMoved && piece2 == null) { //If hasn't moved and space two in front is empty, allow moving double
                pseudoMoves.add(new Move(this, from, new Position(from.row + 2 * direction, from.col), null));
            }
            
        }
        //Capturing diagonally
        piece1 = board.pieceThere(from.row + direction, from.col - 1);
        piece2 = board.pieceThere(from.row + direction, from.col + 1);

        if(piece1 != null || piece2 != null) { //If pieces are diagonal
            if(piece1 != null && !piece1.color.equals(this.color)) { //If enemy
                Move newMove = new Move(this, from, new Position(from.row + direction, from.col - 1), piece1);
                if(onPromotionRank) { //auto-promote to queen
                    newMove.promotionType = "queen";
                }
                pseudoMoves.add(newMove);
            }
            if(piece2 != null && !piece2.color.equals(this.color)) { //If enemy
                Move newMove = new Move(this, from, new Position(from.row + direction, from.col + 1), piece2);
                if(onPromotionRank) { //auto-promote to queen
                    newMove.promotionType = "queen";
                }
                pseudoMoves.add(newMove);
            }

        }

        //En Passant
        Move prevMove = board.getPreviousMove();
        if(prevMove != null && prevMove.piece instanceof Pawn) { //Previous move was from pawn
            if(Math.abs(prevMove.end.row - prevMove.start.row) == 2 && prevMove.end.row == from.row) {//On same row
                if(Math.abs(prevMove.end.col - from.col) == 1) { //Offset column by 1
                    pseudoMoves.add(new Move(this, from, new Position( //Add en passant move
                        from.row + direction,
                        prevMove.end.col),
                        prevMove.piece, prevMove.end));
                }
            }
        }

        return pseudoMoves;
    }
}
