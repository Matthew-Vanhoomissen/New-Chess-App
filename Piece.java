import java.util.ArrayList;

public abstract class Piece {
    public String color;
    public Position position;

    public Piece(String color, Position position) {
        this.color = color;
        this.position = position;
    }

    public String getColor() {
        return color;
    }
    public Position getPosition() {
        return position;
    }

    public abstract ArrayList<Move> getPseudoLegalMoves(Board board);
}