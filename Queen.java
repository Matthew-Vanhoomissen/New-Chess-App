import java.util.ArrayList;

public class Queen extends Piece{
    String image;

    public Queen(String color, Position position) {
        super(color, position);
        if(color.equals("white")) {
            image = "white piece";
        }
        else {
            image = "black piece";
        }
    }

    public ArrayList<Move> getPseudoLegalMoves(Board board) {
        return null;
    }
}
