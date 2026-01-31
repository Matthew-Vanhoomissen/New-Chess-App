import java.util.ArrayList;

public class Queen extends Piece{
    String image;

    public Queen(String color) {
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
