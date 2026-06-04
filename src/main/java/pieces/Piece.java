package pieces;

/**
 * Piece parent class that containts information about piece like
 * color, type, and if it has moved
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.ArrayList;

import game.*;


public abstract class Piece {
    public String color;
    public String type; //Pawn, King, Queen, etc
    public boolean hasMoved;

    public Piece(String color,String type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    public void setMoved(boolean val) {
        this.hasMoved = val;
    }


    /**
     * Returns all pseudo-legal moves for this piece from the given position.
     *
     * <p>Pseudo-legal moves are geometrically valid for the piece type but do
     * not guarantee the resulting position is legal — the king may still be
     * in check after the move. Legal filtering is handled externally either
     * via {@link game.Board#getLegalMoves(Position)} or inline during search
     * via {@link game.Board#getAllPseudoMoves(String)}.
     *
     * @param board the current board state used to check occupancy
     * @param from  the square this piece currently occupies
     * @return a list of pseudo-legal moves, empty if completely blocked
     */
    public abstract ArrayList<Move> getPseudoLegalMoves(Board board, Position from);
}