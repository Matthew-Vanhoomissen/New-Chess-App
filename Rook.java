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

    public ArrayList<Move> getPseudoLegalMoves(Board board) {
        return null;
    }
}
