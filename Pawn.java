import java.util.ArrayList;

public class Pawn extends Piece{
    String image;

    public Pawn(String color) {
        super(color);
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
