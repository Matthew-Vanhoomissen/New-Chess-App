package pieces;
import java.util.ArrayList;

import game.*;


public abstract class Piece {
    public String color;
    public String type;
    public boolean hasMoved;

    public Piece(String color,String type) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    public void setMoved(boolean val) {
        this.hasMoved = val;
    }


    public abstract ArrayList<Move> getPseudoLegalMoves(Board board, Position from);
}