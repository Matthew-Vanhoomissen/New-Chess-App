import java.util.ArrayList;

public class Rook extends Piece{
    String image;

    public Rook(String color) {
        super(color);
        if(color.equals("white")) {
            image = "white piece";
        }
        else {
            image = "black piece";
        }
    }

    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();
        for (int r = from.row - 1; r >= 0; r--) {
            Piece piece = board.pieceThere(r, from.col);

            if (piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(r, from.col)));
            } else {
                if (!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(r, from.col)));
                }
                break;
            }
        }
        for (int r = from.row + 1; r <= 7; r++) {
            Piece piece = board.pieceThere(r, from.col);

            if (piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(r, from.col)));
            } else {
                if (!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(r, from.col)));
                }
                break;
            }
        }
        for (int c = from.col - 1; c >= 0; c--) {
            Piece piece = board.pieceThere(from.row, c);

            if (piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(from.row, c)));
            } else {
                if (!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(from.row, c)));
                }
                break;
            }
        }
        for (int c = from.col + 1; c <= 7; c++) {
            Piece piece = board.pieceThere(from.row, c);

            if (piece == null) {
                pseudoMoves.add(new Move(this, from, new Position(from.row, c)));
            } else {
                if (!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(from.row, c)));
                }
                break;
            }
        }
        return pseudoMoves;
    }
}
