import java.util.ArrayList;

public class Bishop extends Piece{
    String type;

    public Bishop(String color) {
        super(color);
        this.type = "bishop";
    }

    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();
        int r = from.row - 1;
        int c = from.col - 1;
        while(r >= 0 && c >= 0) {
            Piece piece = board.pieceThere(r, c);
            if(piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(r, c)));
            }
            else {
                if(!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(r, c)));
                }
                break;
            }
            r--;
            c--;
        }
        r = from.row - 1; c = from.col + 1;
        while(r >= 0 && c <= 7) {
            Piece piece = board.pieceThere(r, c);
            if(piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(r, c)));
            }
            else {
                if(!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(r, c)));
                }
                break;
            }
            r--;
            c++;
        }
        r = from.row + 1; c = from.col + 1;
        while(r <= 7 && c <= 7) {
            Piece piece = board.pieceThere(r, c);
            if(piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(r, c)));
            }
            else {
                if(!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(r, c)));
                }
                break;
            }
            r++;
            c++;
        }
        r = from.row + 1; c = from.col - 1;
        while(r <= 7 && c >= 0) {
            Piece piece = board.pieceThere(r, c);
            if(piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(r, c)));
            }
            else {
                if(!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(r, c)));
                }
                break;
            }
            r++;
            c--;
        }
        return pseudoMoves;
    }
}
