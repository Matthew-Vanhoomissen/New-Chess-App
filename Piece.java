import java.util.ArrayList;

public abstract class Piece {
    public String color;
    public String type;

    public Piece(String color,String type) {
        this.color = color;
        this.type = type;
    }


    public abstract ArrayList<Move> getPseudoLegalMoves(Board board, Position from);
}