package pieces;

/**
 * Rook specific movement
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.ArrayList;

import game.*;


public class Rook extends Piece{
    String type;

    public Rook(String color) {
        super(color, "rook");
    }

    /**
     * Returns all pseudo-legal moves for this rook from the given position.
     *
     * Generates moves along all four line rays until the edge of the
     * board or a blocking piece is reached. If the blocking piece is an enemy,
     * that square is included as a capture. If it is friendly, the ray stops
     * before it.
     *
     *
     * @param board the current board state used to check occupancy
     * @param from  the square this rook currently occupies
     * @return a list of pseudo-legal moves, empty if completely blocked
     */
    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();

        int[][] directions = { //Four possible directions
            {-1, 0}, {1, 0},
            {0, -1}, {0, 1}
        };

        for(int[] coord : directions) {
            int r = from.row + coord[0]; //Row and column directions
            int c = from.col + coord[1];

            while(r < 8 && r >= 0 && c < 8 && c >= 0) { //While in bounds
                Piece piece = board.pieceThere(r, c);
                if (piece == null) {
                    pseudoMoves.add(new Move(this, from, new Position(r, c), null));
                } else { //Stop when meeting another piece
                    if (!piece.color.equals(this.color)) { //If enemy add
                        pseudoMoves.add(new Move(this, from, new Position(r, c), piece));
                    }
                    break;
                }
                r += coord[0]; //Increment each index
                c += coord[1];
            } 
        }
        return pseudoMoves;
    }
}
