public class Move {
    public Piece piece;
    public Position start;
    public Position end;

    public Move(Piece piece, Position start, Position end) {
        this.piece = piece;
        this.start = start;
        this.end = end;
    }

    public String toString() {
        return end.row + " " + end.col;
    }
}
