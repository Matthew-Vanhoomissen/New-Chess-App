package ml;

/**
 * Contains all methods to have chess bot choose the best move given the board as 
 * an input. Uses minmax algorithm with neural network and other optimizing features
 * 
 * @author Matthew-Vanhoomissen
 */

import game.*;
import pieces.*;
import transposition.*;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

public class ChessEvaluator {
    private MultiLayerNetwork model;
    private NNEvaluator nnEvaluator;
    private HashMap<Long, TTEntry> transpositionTable = new HashMap<>();

    // Runtime constructor — lightweight, no DL4J
    public ChessEvaluator(NNEvaluator nnEvaluator) {
        this.nnEvaluator = nnEvaluator;
    }

    // Export constructor — only use once to generate chess_weights.bin
    public ChessEvaluator(MultiLayerNetwork model) {
        this.model = model;
    }

    //Loads raw weights from .bin file
    public static ChessEvaluator loadForRuntime(String weightsPath) throws IOException {
        return new ChessEvaluator(new NNEvaluator(weightsPath));
    }

    /**
     * Writes model to .zip file
     * 
     * @param path
     * @throws IOException
     */
    public void save(String path) throws IOException {
        ModelSerializer.writeModel(model, path, true);
    }

    /**
     * Loads model from .zip file and returns the evaluator object.
     * 
     * @param modelPath
     * @return evaluator
     * @throws IOException
     */
    public static ChessEvaluator load(String path) throws IOException {
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(path);
        return new ChessEvaluator(model);
    }

    /**
     * Reads input .zip model and creates raw weight .bin file to
     * do forward propogation without DL4J overhead for better time
     * complexity. 
     * 
     * Used in {@link ml.NNEvaluator#evaluate(Board)}
     * 
     * @param path
     * @throws IOException
     */
    public void exportWeights(String path) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(path)))) {
            
            for (int i = 0; i < model.getnLayers(); i++) {
                float[] weights = model.getLayer(i).getParam("W").dup().data().asFloat();
                float[] biases  = model.getLayer(i).getParam("b").dup().data().asFloat();
                
                // Write dimensions then data
                dos.writeInt(weights.length);
                for (float w : weights) dos.writeFloat(w);
                dos.writeInt(biases.length);
                for (float b : biases) dos.writeFloat(b);
            }
        }
        System.out.println("Weights exported to " + path);
    }

    /**
     * Iterates through all possible moves within a depth of 4 and selects
     * the move that grants the model the highest advantage. Uses iterative 
     * deepening to populate the transposition table which vastly cuts computation
     * time by instantly processing duplicates. Has time limiting if processing
     * takes too long and returns the best move found so far. 
     * 
     * @param board current state
     * @param aiColor ai turn
     * @return best move
     */
    public Move getAIMove(Board board, String aiColor) {
        transpositionTable.clear(); //Clear table to accurately store board duplicates
        Move bestMove = null;
        boolean isMaximizing = aiColor.equals("white"); //White always maximizes even if it is black turn
        long startTime = System.currentTimeMillis();

        //Start at depth 1 and go to 4. Top down computation
        for (int depth = 1; depth <= 4; depth++) {
            Move candidate = searchRoot(board, aiColor, isMaximizing, depth); //Best move from root
            if (candidate != null) bestMove = candidate;
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Depth " + depth + " done in " + elapsed + "ms");
            if (elapsed > 20000) break; // stop if taking too long
        }
        return bestMove;
    }

    /**
     * Evaluates every possible next valid move and using the minimax 
     * algorithm, continues to the leaf nodes and evaluates the team advantage.
     * If that score is better than the best score, set it equal to new score.
     * 
     * @param board current state
     * @param aiColor
     * @param isMaximizing white maximizes black minimizes
     * @param depth limit on how far to look
     * @return best move
     */
    private Move searchRoot(Board board, String aiColor, boolean isMaximizing, int depth) {
        ArrayList<Move> moves = board.getAllPseudoMoves(aiColor); //All possible moves for the color
        Move bestMove = null;
        float bestScore = isMaximizing ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;

        // Use full window at root — don't tighten alpha/beta between moves
        for (Move m : orderMoves(moves, board)) {
            board.makeMove(m);
            if (board.isKingInCheck(aiColor)) { board.undoMove(m); continue; } //Ensure move is valid

            float val = minimax(board, depth - 1,     // decrease depth by 1
                            Float.NEGATIVE_INFINITY,  // fresh window each move
                            Float.POSITIVE_INFINITY, 
                            !isMaximizing);           // switch team move
            board.undoMove(m);

            if (isMaximizing ? val > bestScore : val < bestScore) { //Save best score and move
                bestScore = val;
                bestMove = m;
            }
        }
        return bestMove;
    }

    /**
     * Minimax algorithm makes every possible move given depth limit and at the leaf node
     * evaluates the board for an advantage float value. Utilizes the transposition table
     * to not repeat previously evaluated boards and get their value with O(1) lookup. If board
     * is new, it is added to the table. 
     * 
     * For even better time complexity, alpha beta pruning is used to prune off sections of the
     * search that would not be useful to compute and does not affect end result.
     * 
     * @param currentState
     * @param depth
     * @param alpha lower bound and is the minimum guaranteed score the maximizing player can be certain of
     * @param beta  upper bound and is the maximum guaranteed score the minimizing player can be certain of
     * @param maximizing
     * @return float value of best leaf node value in this search
     */
    private float minimax(Board currentState, int depth, float alpha, float beta, boolean maximizing) {
        // Look up the current position in the transposition table using its Zobrist hash.
        // Only use the cached result if it was searched at least as deep as the current
        // request — a shallower cached result may miss tactics visible at this depth.
        long hash = currentState.getZobristHash();
        TTEntry cached = transpositionTable.get(hash);
        if (cached != null && cached.depth >= depth) {

            // EXACT — full search was completed with no cutoffs, score is precise
            if (cached.flag == TTEntry.EXACT) return cached.score;

            // LOWER — a beta cutoff occurred, real score is >= cached.score.
            // Tighten alpha upward since we know the position is at least this good.
            if (cached.flag == TTEntry.LOWER) alpha = Math.max(alpha, cached.score);

            // UPPER — every move failed low, real score is <= cached.score.
            // Tighten beta downward since we know the position is at most this good.
            if (cached.flag == TTEntry.UPPER) beta = Math.min(beta, cached.score);

            // If the window has collapsed after tightening, this node can be pruned —
            // the opponent already has a refutation regardless of the exact score.
            if (beta <= alpha) return cached.score;
        }

        String currColor = maximizing ? "white" : "black"; // whites perspective, maximizing = white

        //If no valid moves then game is over.        
        ArrayList<Move> allTeamMoves = currentState.getAllPseudoMoves(currColor);
        if (allTeamMoves.isEmpty()) {
            return currentState.isKingInCheck(currColor)
                ? (maximizing ? -1.0f : 1.0f) //Checkmate against you is the worst possible move
                : 0.0f; //Stalemate           //Avoid at all cost
        }

        // Leaf node evaluation
        if (depth == 0) {
            //float score = nnEvaluator.evaluate(currentState);
            float score = hybridEvaluate(currentState);
            transpositionTable.put(hash, new TTEntry(score, 0, TTEntry.EXACT, null));
            return score;
        }
        //Order moves for more efficient alpha beta pruning
        List<Move> ordered = orderMoves(allTeamMoves, currentState);
        // Maximizing player (white) — find the move that produces the highest score.
        // Start pessimistically at negative infinity so any legal move improves it.
        // Flag starts as UPPER assuming no move will raise alpha (fail-low node).
        if (maximizing) {
            float bestScore = Float.NEGATIVE_INFINITY;
            int flag = TTEntry.UPPER;
            Move bestMove = null;
            boolean hasMoved = false;

            for (Move m : ordered) {
                // Castling requires additional legality checks beyond pseudo-legality —
                // verify path is clear and king does not pass through check.
                if (m.castleMove) {
                    if (!currentState.castleCheck(currColor, m)) continue;
                }

                currentState.makeMove(m);

                // Filter illegal pseudo-legal moves — skip any that leave the king in check.
                if (currentState.isKingInCheck(currColor)) {
                    currentState.undoMove(m);
                    continue;
                }

                hasMoved = true;
                float score = minimax(currentState, depth - 1, alpha, beta, !maximizing);
                currentState.undoMove(m);

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = m;

                    if (score > alpha) {
                        // This move raised alpha — we have a new lower bound on the score.
                        // Flag becomes EXACT since at least one move improved the position.
                        alpha = score;
                        flag = TTEntry.EXACT;
                    }
                }

                // Beta cutoff — the minimizing player already has a move elsewhere
                // in the tree that is better than anything we can achieve here. Stop
                // searching remaining moves and mark as LOWER bound (score >= bestScore).
                if (beta <= alpha) {
                    flag = TTEntry.LOWER;
                    break;
                }
            }

            // No legal moves found after filtering — either checkmate or stalemate.
            // Checkmate scores are oriented relative to the maximizing player:
            // maximizing in checkmate = -1.0 (loss), minimizing in checkmate = +1.0 (win).
            if (!hasMoved) {
                return currentState.isKingInCheck(currColor)
                    ? (maximizing ? -1.0f : 1.0f)
                    : 0.0f; // stalemate
            }

            // Store result in transposition table for future lookups at this position.
            transpositionTable.put(hash, new TTEntry(bestScore, depth, flag, bestMove));
            return bestScore;
        }
        // Minimizing player (black) — find the move that produces the lowest score.
        // Start pessimistically at positive infinity so any legal move improves it.
        // Flag starts as UPPER assuming no move will raise beta (fail-high node).
        else { //Minimizing score
            float lowestScore = Float.POSITIVE_INFINITY;
            int flag = TTEntry.UPPER;
            Move worstMove = null;
            boolean hasMoved = false;

            for(Move m : ordered) {
                // Castling requires additional legality checks beyond pseudo-legality —
                // verify path is clear and king does not pass through check.
                if(m.castleMove) {
                    if(!currentState.castleCheck(currColor, m)) {
                        continue;
                    }
                }
                currentState.makeMove(m);
                // Filter illegal pseudo-legal moves — skip any that leave the king in check.
                if(currentState.isKingInCheck(currColor)) {
                    currentState.undoMove(m);
                    continue;
                }
                hasMoved = true;
                float score = minimax(currentState, depth - 1, alpha, beta, !maximizing);
                currentState.undoMove(m);
                if (score < lowestScore) {
                    lowestScore = score;
                    worstMove = m;
                    if (score < beta) {
                        // This move raised beta — we have a new higher bound on the score.
                        // Flag becomes EXACT since at least one move improved the position.
                        beta = score;
                        flag = TTEntry.EXACT;
                    }
                }
                // Beta cutoff — the maximizing player already has a move elsewhere
                // in the tree that is better than anything we can achieve here. Stop
                // searching remaining moves and mark as UPPER bound (score < bestScore).
                if(beta <= alpha) {
                    flag = TTEntry.UPPER;
                    break;
                }
            }
            // No legal moves found after filtering — either checkmate or stalemate.
            // Checkmate scores are oriented relative to the maximizing player:
            // maximizing in checkmate = -1.0 (loss), minimizing in checkmate = +1.0 (win).
            if (!hasMoved) {
                return currentState.isKingInCheck(currColor)
                    ? (maximizing ? -1.0f : 1.0f)
                    : 0.0f;
            }
            // Store result in transposition table for future lookups at this position.
            transpositionTable.put(hash, new TTEntry(lowestScore, depth, flag, worstMove));
            return lowestScore;
        }
    } 

    /**
     * Helper method that returns valuation of each piece in chess. If piece
     * is a king or null, retrun 0 since that value is not necessary.
     * 
     * @param piece input
     * @return int value
     */
    private int pieceValue(Piece piece) {
        if(piece == null || piece instanceof King) { return 0; }
        if(piece instanceof Pawn) { return 1; }
        if(piece instanceof Knight) { return 3; }
        if(piece instanceof Bishop) { return 3; }
        if(piece instanceof Rook) { return 5; }
        if(piece instanceof Queen) { return 9; }
        throw new IllegalArgumentException("Unknown piece type");
    }

    /**
     * Gives a value for capturing move. Purpose is to ward off
     * too many trades when you can capture with worse piece.
     * 
     * @param m
     * @return
     */
    private int mvvLva(Move m) {
        if (m.capturedPiece == null) return 0;
        // Prioritize capturing high value pieces with low value pieces
        return pieceValue(m.capturedPiece) * 10 
            - pieceValue(m.piece);
    }

    /**
     * Order the possible movves by the mvvLva values. If there is already 
     * a cached best move start with that.
     * 
     * @param moves unsorted array
     * @param board
     * @return ordered array of moves
     */
    private List<Move> orderMoves(ArrayList<Move> moves, Board board) {
        List<Move> copy = new ArrayList<>(moves);
        TTEntry cached = transpositionTable.get(board.getZobristHash());
        Move ttBestMove = (cached != null) ? cached.bestMove : null;

        copy.sort((a, b) -> {
            // TT best move always first
            if (ttBestMove != null) {
                if (a.equals(ttBestMove)) return -1;
                if (b.equals(ttBestMove)) return 1;
            }
            // Then MVV-LVA for captures
            return mvvLva(b) - mvvLva(a);
        });
        return copy;
    }

    /**
     * Evaluates board through the neural network and on material score.
     * Chooses material score if close to equal position and there is 
     * significant material change. This is necessary due to inaccuracies 
     * in the neural network evaluation.
     * 
     * @param currentState of the board
     * @return float value of position
     */
    private float hybridEvaluate(Board currentState) {
        float nnScore = nnEvaluator.evaluate(currentState);

        // If NN thinks position is roughly equal, verify with material
        if (Math.abs(nnScore) < 0.15f) {
            float materialScore = getMaterialScore(currentState);

            // If material is significantly imbalanced despite NN saying equal,
            // blend toward the material score
            if (Math.abs(materialScore) > 0.05f) {
                return 0.5f * nnScore + 0.5f * materialScore;
            }
        }
        return nnScore;
    }

    /**
     * Adds up material values of each team and returns the difference
     * 
     * @param board
     * @return team difference value
     */
    private float getMaterialScore(Board board) {
        float score = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = board.pieceThere(i, j);
                if (p == null) continue;
                float val = pieceValue(p);
                score += p.color.equals("white") ? val : -val;
            }
        }
        return Math.max(-1.0f, Math.min(1.0f, score / 39.0f));
    }
    
}