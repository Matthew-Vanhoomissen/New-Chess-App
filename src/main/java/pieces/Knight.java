package pieces;

/**
 * Knight specific movement
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.ArrayList;

import game.*;


public class Knight extends Piece{
    String type;

    public Knight(String color) {
        super(color, "knight");
    }

    /**
     * Returns all pseudo-legal moves for this knight from the given position.
     *
     * Generates moves for each of the eight possible positions any knight can 
     * go to. Adds to pseudo-legal moves if empty or contains an enemy piece.
     *
     *
     * @param board the current board state used to check occupancy
     * @param from  the square this knight currently occupies
     * @return a list of pseudo-legal moves, empty if completely blocked
     */
    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();
        int[][] offsets = { //The eight possible directions to move
            {-2, -1}, {-2,  1},
            {-1, -2}, {-1,  2},
            { 1, -2}, { 1,  2},
            { 2, -1}, { 2,  1}
        };

        for (int[] offset : offsets) {
            int newRow = from.row + offset[0];
            int newCol = from.col + offset[1];

            if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8) { //Check bounds
                continue;
            }

            Piece piece = board.pieceThere(newRow, newCol);

            if (piece == null) { //If space empty add it
                pseudoMoves.add(new Move(this, from, new Position(newRow, newCol), null));
            }
            else if (!piece.color.equals(this.color)) { //If not empty but is opponent, capture
                pseudoMoves.add(new Move(this, from, new Position(newRow, newCol), piece));
            }
        }
        return pseudoMoves;
    }
}
