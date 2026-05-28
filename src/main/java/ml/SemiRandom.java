package ml;

import java.util.ArrayList;

import game.*;
import pieces.*;

public class SemiRandom {
    public static Move getRandomMove(Board board, String color) {
        ArrayList<Move> totalMoves = board.getAllTeamMoves(color);
        if(totalMoves.isEmpty()) { return null; }

        if(Math.random() < .3) {
            Move bestCapture = bestCapture(totalMoves);
            if(bestCapture != null) { return bestCapture; }
        }
        int randIndex = (int) Math.floor(Math.random() * totalMoves.size());
        return totalMoves.get(randIndex);
        
    }

    private static Move bestCapture(ArrayList<Move> moves) {
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

    public static float getBoardValue(Board board) {
        float score = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board.pieceThere(i, j);
                if (piece == null || piece instanceof King) continue;
                float value = pieceValue(piece);
                score += piece.color.equals("white") ? value : -value;
            }
        }
        // Normalize to roughly -1 to 1 
        return Math.max(-1.0f, Math.min(1.0f, score / 39.0f));
    }

    public static int pieceValue(Piece piece) {
        if(piece == null || piece instanceof King) { return 0; }
        if(piece instanceof Pawn) { return 1; }
        if(piece instanceof Knight) { return 3; }
        if(piece instanceof Bishop) { return 3; }
        if(piece instanceof Rook) { return 5; }
        if(piece instanceof Queen) { return 9; }
        throw new IllegalArgumentException("Unknown piece type");
    }

}
