public class Move {
    public Piece piece;
    public Piece capturedPiece;
    public Position start;
    public Position end;
    public boolean firstMove;
    

    public Move(Piece piece, Position start, Position end, Piece capturedPiece) {
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.capturedPiece = capturedPiece;
    }

    public String toString() {
        return end.row + " " + end.col;
    }
}
