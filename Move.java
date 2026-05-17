public class Move {
    public Piece piece;
    public Piece capturedPiece;
    public Piece enPassantPiece;
    public Position enPassantPosition;
    public Position start;
    public Position end;
    public boolean firstMove;
    public boolean enPassantMove;
    

    public Move(Piece piece, Position start, Position end, Piece capturedPiece) {
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.capturedPiece = capturedPiece;
        this.enPassantMove = false;
    }

    public Move(Piece piece, Position start, Position end, Piece enPassantPiece, Position enPassantPosition) {
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.enPassantPiece = enPassantPiece;
        this.enPassantPosition = enPassantPosition;
        this.enPassantMove = true;
    }


    public String toString() {
        return end.row + " " + end.col;
    }
}
