package pieces;

/**
 * King specific movement
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.ArrayList;

import game.*;


public class King extends Piece{
    String type;

    public King(String color) {
        super(color, "king");
    }

    /**
     * Returns all pseudo-legal moves for this king from the given position.
     *
     * Generates moves for every neighboring square which includes empty squares
     * or squares with an enemy piece. To check for castling, it ensures the rook
     * is in position and all squares between are empty.
     *
     *
     * @param board the current board state used to check occupancy
     * @param from  the square this king currently occupies
     * @return a list of pseudo-legal moves, empty if completely blocked
     */
    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();
        //All neightboring squares
        int[][] directions = {
            {1,-1}, {1,1},{1,0},
            {0,1}, {0,-1},
            {-1,-1}, {-1,0}, {-1,1}
        };

        for(int[] coord : directions) { 
            if( coord[0] + from.row > 7 || 
                coord[0] + from.row < 0 || 
                coord[1] + from.col > 7 || 
                coord[1] + from.col < 0) { //Ensure in bounds
                continue;
            }
            Piece piece = board.pieceThere(from.row + coord[0], from.col + coord[1]);

            if(piece == null) {//If the square is empty
                pseudoMoves.add(new Move(this, from, new Position(from.row + coord[0], from.col + coord[1]), null));
            }
            else if(!piece.color.equals(this.color)) { //Add if its an opponent piece
                pseudoMoves.add(new Move(this, from, new Position(from.row + coord[0], from.col + coord[1]), piece));
            }
        }

        //Castling

        if(this.hasMoved == false) { //To castle, the king can't have moved
            //Short castle
            int c = from.col + 1; //Start one to the right
            if(board.pieceThere(from.row, c++) == null && board.pieceThere(from.row, c++) == null) { //If both squares are empty
                Piece piece = board.pieceThere(from.row, c);
                if(piece != null && piece instanceof Rook && piece.color.equals(this.color) && !piece.hasMoved) { //And there is a rook that hasn't moved
                    //Add castling move
                    pseudoMoves.add(new Move(
                        this, from, new Position(from.row, from.col + 2), 
                        piece, new Position(from.row, c), new Position(from.row, c - 2)));
                }
            }
            
            //Long castle
            c = from.col - 1; //Start one to the left
            if(board.pieceThere(from.row, c--) == null && //If space between king and rook are empty
               board.pieceThere(from.row, c--) == null && 
               board.pieceThere(from.row, c--) == null) {
                Piece piece = board.pieceThere(from.row, c);
                if(piece != null && piece instanceof Rook && piece.color.equals(this.color) && !piece.hasMoved) { //If there is a rook that hasn't moved
                    //Add castling move
                    pseudoMoves.add(new Move(
                        this, from, new Position(from.row, from.col - 2), 
                        piece, new Position(from.row, c), new Position(from.row, c + 3)));
                }
            }
        }
        

        return pseudoMoves;
    }
}
