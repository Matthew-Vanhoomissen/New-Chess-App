package ml;

import game.*;
import pieces.*;

public class BoardEncoder {
    public static float[] convertBoard(Board board) {
        float[] input = new float[781];
        //Input all colored piece positions
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                Piece piece = board.pieceThere(i, j);
                if(piece == null) { continue; }

                int boardIndex = i * 8 + j;
                int planeIndex = getPlaneIndex(piece);
                input[boardIndex + planeIndex * 64] = 1.0f;
            }
        }

        //Input extra identifiers
        int base = 768;
        input[base + 1] = board.kingSideCastle("white")  ? 1.0f : 0.0f;
        input[base + 2] = board.queenSideCastle("white") ? 1.0f : 0.0f;
        input[base + 3] = board.kingSideCastle("black")  ? 1.0f : 0.0f;
        input[base + 4] = board.queenSideCastle("black") ? 1.0f : 0.0f;

        // En passant file (one-hot, indices 773-780)
        Move lastMove = board.prevMove;
        if (lastMove != null && lastMove.piece instanceof Pawn) {
            int movedTwo = Math.abs(lastMove.end.row - lastMove.start.row);
            if (movedTwo == 2) {
                input[base + 5 + lastMove.end.col] = 1.0f;
            }
        }

        return input;

    }

    private static int getPlaneIndex(Piece piece) {
        int colorOffset = piece.color.equals("white") ? 0 : 6;
        if (piece instanceof Pawn)   return colorOffset + 0;
        if (piece instanceof Knight) return colorOffset + 1;
        if (piece instanceof Bishop) return colorOffset + 2;
        if (piece instanceof Rook)   return colorOffset + 3;
        if (piece instanceof Queen)  return colorOffset + 4;
        if (piece instanceof King)   return colorOffset + 5;
        throw new IllegalArgumentException("Unknown piece type");
    }
}
