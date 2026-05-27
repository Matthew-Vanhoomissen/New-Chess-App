package ml;

import java.util.ArrayList;

import game.*;
import pieces.*;

public class SemiRandom {
    public Move getRandomMove(Board board, String color) {
        ArrayList<Move> totalMoves = board.getAllTeamMoves(color);
        if(totalMoves.isEmpty()) { return null; }

        if(Math.random() < .3) {
            Move bestCapture = bestCapture(totalMoves);
            if(bestCapture != null) { return bestCapture; }
        }
        int randIndex = (int) Math.floor(Math.random() * totalMoves.size());
        return totalMoves.get(randIndex);
        
    }

    private Move bestCapture(ArrayList<Move> moves) {
        Move best = null;
        int bestVal = -1;
        for(Move m : moves) {
            if(m.capturedPiece == null) { 
                if(m.enPassantMove) {
                    if(1 > bestVal) {
                        best = m;
                    }
                }
                continue; 
            }
            int val = pieceValue(m.capturedPiece);
            if(val > bestVal) {
                best = m;
            }
        }
        return best;
    }

    private int pieceValue(Piece piece) {
        if(piece instanceof Pawn) { return 1; }
        if(piece instanceof Knight) { return 3; }
        if(piece instanceof Bishop) { return 3; }
        if(piece instanceof Rook) { return 5; }
        if(piece instanceof Queen) { return 9; }
        throw new IllegalArgumentException("Unknown piece type");
    }
}
