package pieces;
import java.util.ArrayList;

import game.*;


public class King extends Piece{
    String type;

    public King(String color) {
        super(color, "king");
    }

    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();
        int[][] directions = {
            {1,-1}, {1,1},{1,0},
            {0,1}, {0,-1},
            {-1,-1}, {-1,0}, {-1,1}
        };

        for(int[] coord : directions) {
            if(coord[0] + from.row > 7 || coord[0] + from.row < 0 || coord[1] + from.col > 7 || coord[1] + from.col < 0) {
                continue;
            }
            Piece piece = board.pieceThere(from.row + coord[0], from.col + coord[1]);
            if(piece != null) {
                if(!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(from.row + coord[0], from.col + coord[1]), piece));
                }
            }
            else {
                pseudoMoves.add(new Move(this, from, new Position(from.row + coord[0], from.col + coord[1]), null));
            }
        }

        //Castling

        if(this.hasMoved == false) {
            //Short castle
            int c = from.col + 1;
            if(board.pieceThere(from.row, c++) == null && board.pieceThere(from.row, c++) == null) {
                Piece piece = board.pieceThere(from.row, c);
                if(piece != null && piece instanceof Rook && piece.color.equals(this.color) && !piece.hasMoved) {
                    pseudoMoves.add(new Move(
                        this, from, new Position(from.row, from.col + 2), 
                        piece, new Position(from.row, c), new Position(from.row, c - 2)));
                }
            }
            
            //Long castle
            c = from.col - 1;
            if(board.pieceThere(from.row, c--) == null && board.pieceThere(from.row, c--) == null && board.pieceThere(from.row, c--) == null) {
                Piece piece = board.pieceThere(from.row, c);
                if(piece != null && piece instanceof Rook && piece.color.equals(this.color) && !piece.hasMoved) {
                    pseudoMoves.add(new Move(
                        this, from, new Position(from.row, from.col - 2), 
                        piece, new Position(from.row, c), new Position(from.row, c + 3)));
                }
            }
        }
        

        return pseudoMoves;
    }
}
