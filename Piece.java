import java.util.ArrayList;

public abstract class Piece {
    public String color;

    public Piece(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public abstract ArrayList<Move> getPseudoLegalMoves(Board board);
}