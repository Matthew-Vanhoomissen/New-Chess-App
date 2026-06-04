package pieces;

/**
 * Bishop specific movement
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.ArrayList;

import game.*;

public class Bishop extends Piece{
    String type;

    public Bishop(String color) {
        super(color, "bishop");
    }

    /**
     * Returns all pseudo-legal moves for this bishop from the given position.
     *
     * Generates moves along all four diagonal rays until the edge of the
     * board or a blocking piece is reached. If the blocking piece is an enemy,
     * that square is included as a capture. If it is friendly, the ray stops
     * before it.
     *
     *
     * @param board the current board state used to check occupancy
     * @param from  the square this bishop currently occupies
     * @return a list of pseudo-legal moves, empty if completely blocked
     */
    @Override
    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();

        // Four diagonal directions: NW, SW, SE, NE
        int[][] directions = {
            {-1, -1}, {1, -1},
            {1,   1}, {-1, 1}
        };

        for (int[] dir : directions) {
            int r = from.row + dir[0];
            int c = from.col + dir[1];

            // Slide along the ray until board edge or blocking piece
            while (r >= 0 && r < 8 && c >= 0 && c < 8) {
                Piece piece = board.pieceThere(r, c);

                if (piece == null) {
                    // Empty square — add quiet move and continue ray
                    pseudoMoves.add(new Move(this, from, new Position(r, c), null));
                } else {
                    // Enemy piece — add capture then stop ray
                    // Friendly piece — stop ray without adding
                    if (!piece.color.equals(this.color)) {
                        pseudoMoves.add(new Move(this, from, new Position(r, c), piece));
                    }
                    break;
                }

                r += dir[0];
                c += dir[1];
            }
        }

        return pseudoMoves;
    }
}