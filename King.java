import java.util.ArrayList;

public class King extends Piece{
    String type;

    public King(String color) {
        super(color);
        this.type = "king";
    }

    public ArrayList<Move> getPseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> pseudoMoves = new ArrayList<>();
        int[][] directions = {
            {1,-1}, {1,1},{1,0},
            {0,1}, {0,-1},
            {-1,-1}, {-1,0}, {-1,1}
        };

        for(int[] coord : directions) {
            if(coord[0] + from.row > 7 || coord[0] + from.row < 0 || coord[1] + from.col > 7 || coord[1] + from.col < 0) {
                continue;
            }
            Piece piece = board.pieceThere(from.row + coord[0], from.col + coord[1]);
            if(piece != null) {
                if(!piece.color.equals(this.color)) {
                    pseudoMoves.add(new Move(this, from, new Position(from.row + coord[0], from.col + coord[1])));
                }
            }
            else {
                pseudoMoves.add(new Move(this, from, new Position(from.row + coord[0], from.col + coord[1])));
            }
        }
        return pseudoMoves;
    }
}
