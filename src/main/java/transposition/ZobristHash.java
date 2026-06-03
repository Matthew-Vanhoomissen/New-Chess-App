package transposition;

/**
 * Translates a board state into hash value so each board has
 * a unique key which is stored in the table
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.Random;

import game.*;
import pieces.*;

public class ZobristHash {
    private static final long[][][] table = new long[2][6][64];
    private static final long blackToMove;

    static {
        Random rand = new Random(123456789L);
        for (int c = 0; c < 2; c++)
            for (int p = 0; p < 6; p++)
                for (int s = 0; s < 64; s++)
                    table[c][p][s] = rand.nextLong();
        blackToMove = rand.nextLong();
    }

    public static long get(Piece piece, int square) {
        int color = piece.color.equals("white") ? 0 : 1;
        int type  = getPieceIndex(piece);
        return table[color][type][square];
    }

    public static long getBlackToMove() { return blackToMove; }

    public static long compute(Board board) {
        long hash = 0L;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                Piece p = board.pieceThere(i, j);
                if (p != null)
                    hash ^= get(p, i * 8 + j);
            }
        if (board.prevMove != null ? board.prevMove.piece.color.equals("black") : false)
            hash ^= blackToMove;
        return hash;
    }

    private static int getPieceIndex(Piece p) {
        if (p instanceof Pawn)   return 0;
        if (p instanceof Knight) return 1;
        if (p instanceof Bishop) return 2;
        if (p instanceof Rook)   return 3;
        if (p instanceof Queen)  return 4;
        if (p instanceof King)   return 5;
        throw new IllegalArgumentException("Unknown piece");
    }
}