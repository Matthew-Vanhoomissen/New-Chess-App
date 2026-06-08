package transposition;

/**
 * Translates a board state into hash value so each board has
 * a unique key which is stored in the table.
 * 
 * Zobrist hashing assigns a unique random 64-bit number to every
 * combination of piece, color, and square. A position's hash is the XOR
 * of all keys for pieces currently on the board plus a side-to-move key.
 * XOR has the property of being its own inverse, so making and undoing a
 * move requires only two XOR operations rather than recomputing from scratch.
 *
 * The fixed random seed ensures the same keys are generated across all
 * runs, so hashes stored in the transposition table remain valid for the
 * lifetime of the program.
 * 
 * @see transposition.TTEntry
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.Random;

import game.*;
import pieces.*;

public class ZobristHash {

    /**
     * Random keys indexed by [color 0-1][pieceType 0-5][square 0-63].
     * Color: 0 = white, 1 = black.
     * Piece type indices are defined by {@link #getPieceIndex(Piece)}.
     * Square index is row * 8 + col.
     */
    private static final long[][][] table = new long[2][6][64];

    /**
     * XORed into the hash when it is black's turn to move.
     * Ensures positions with the same pieces but different side-to-move
     * produce distinct hash values.
     */
    private static final long blackToMove;

    // Populate all keys once at class load time using a fixed seed.
    // A fixed seed guarantees identical keys across all runs of the program.
    static {
        Random rand = new Random(123456789L);
        for (int c = 0; c < 2; c++)
            for (int p = 0; p < 6; p++)
                for (int s = 0; s < 64; s++)
                    table[c][p][s] = rand.nextLong();
        blackToMove = rand.nextLong();
    }

    /**
     * Returns the Zobrist key for a specific piece on a specific square.
     *
     * Used by {@link game.Board#makeMove} and {@link game.Board#undoMove}
     * to incrementally update the running hash. XORing this key in adds the
     * piece; XORing it again removes it, since XOR is its own inverse.
     *
     * @param piece  the piece to look up
     * @param square the square index (row * 8 + col), range 0–63
     * @return the 64-bit Zobrist key for this piece/square combination
     */
    public static long get(Piece piece, int square) {
        int color = piece.color.equals("white") ? 0 : 1;
        int type  = getPieceIndex(piece);
        return table[color][type][square];
    }

    /**
     * Returns the Zobrist key for the side-to-move component.
     *
     * XORed into the hash when it is black's turn. Called by
     * {@link game.Board#switchTurn()} during null move pruning and by
     * {@link game.Board#makeMove}/{@link game.Board#undoMove} to keep
     * the side-to-move component of the hash in sync.
     *
     * @return the 64-bit key representing black to move
     */
    public static long getBlackToMove() { return blackToMove; }

    /**
     * Computes the Zobrist hash from scratch for the current board state.
     *
     * Iterates all 64 squares and XORs in the key for each occupied square,
     * then XORs in the side-to-move key if it is black's turn. This is an
     * O(64) operation and should only be called during board initialization
     * via {@link game.Board#initZobrist()}. During search, use the incrementally
     * maintained hash via {@link game.Board#getZobristHash()} instead.
     *
     * @param board the board to hash
     * @return the 64-bit Zobrist hash of the current position
     */
    public static long compute(Board board) {
        long hash = 0L;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                Piece p = board.pieceThere(i, j);
                if (p != null)
                    hash ^= get(p, i * 8 + j);
            }
        // XOR in side-to-move key if it is black's turn
        if (board.prevMove != null && board.prevMove.piece.color.equals("black"))
            hash ^= blackToMove;
        return hash;
    }

    /**
     * Maps a piece to its index in the Zobrist table.
     *
     * Indices are fixed and must remain consistent with the table
     * dimensions — changing these values would invalidate all stored hashes.
     *
     * @param p the piece to index
     * @return an integer index in the range 0–5
     * @throws IllegalArgumentException if the piece type is unrecognized
     */
    private static int getPieceIndex(Piece p) {
        if (p instanceof Pawn)   return 0;
        if (p instanceof Knight) return 1;
        if (p instanceof Bishop) return 2;
        if (p instanceof Rook)   return 3;
        if (p instanceof Queen)  return 4;
        if (p instanceof King)   return 5;
        throw new IllegalArgumentException("Unknown piece type: " + p.getClass().getName());
    }
}