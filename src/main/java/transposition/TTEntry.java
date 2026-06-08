package transposition;

/**
 * TTEntry is the hash object contained in the transposition table to store
 * already known inputs and outputs for faster runtime of minmax algorithm
 * and alpha beta pruning
 * 
 * @author Matthew-Vanhoomissen
 */

import game.Move;

public class TTEntry {
    public static final int EXACT = 0;  // exact score
    public static final int LOWER = 1;  // alpha cutoff (lower bound)
    public static final int UPPER = 2;  // beta cutoff (upper bound)

    public float score;  //Value of board state
    public int depth;    //Where this board state lies in the minimax search
    public int flag;     //How the score was produced ie. EXACT means no cutoffs and true score.
                         //LOWER means the true score is >= cached score
                         //HIGHER means the true score is <= cahced score

    public Move bestMove;/**Best move stored so far for this board state. This is utilized when sorting
                            the best moves so the best move goes first via {@link ml.ChessEvaluator#orderMoves(java.util.ArrayList, game.Board)} */

    public TTEntry(float score, int depth, int flag, Move bestMove) {
        this.score = score;
        this.depth = depth;
        this.flag = flag;
        this.bestMove = bestMove;
    }
}