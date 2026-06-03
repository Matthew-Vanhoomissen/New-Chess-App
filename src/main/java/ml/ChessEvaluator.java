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
import java.util.stream.Collectors;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.bindings.Nd4jCpu.prelu;

public class ChessEvaluator {
    private MultiLayerNetwork model;
    private NNEvaluator nnEvaluator;
    private HashMap<Long, TTEntry> transpositionTable = new HashMap<>();
    private static final int NULL_MOVE_REDUCTION = 2;

    // Runtime constructor — lightweight, no DL4J
    public ChessEvaluator(NNEvaluator nnEvaluator) {
        this.nnEvaluator = nnEvaluator;
    }

    // Export constructor — only use once to generate chess_weights.bin
    public ChessEvaluator(MultiLayerNetwork model) {
        this.model = model;
    }

    public static ChessEvaluator loadForRuntime(String weightsPath) throws IOException {
        return new ChessEvaluator(new NNEvaluator(weightsPath));
    }

    public static ChessEvaluator loadForExport(String modelPath) throws IOException {
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(modelPath);
        return new ChessEvaluator(model);
    }

    public void save(String path) throws IOException {
        ModelSerializer.writeModel(model, path, true);
    }

    public static ChessEvaluator load(String path) throws IOException {
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(path);
        return new ChessEvaluator(model);
    }

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

    public Move getAIMove(Board board, String aiColor) {
        transpositionTable.clear();
        Move bestMove = null;
        boolean isMaximizing = aiColor.equals("white");
        long startTime = System.currentTimeMillis();

        for (int depth = 1; depth <= 4; depth++) {
            Move candidate = searchRoot(board, aiColor, isMaximizing, depth);
            if (candidate != null) bestMove = candidate;
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.println("Depth " + depth + " done in " + elapsed + "ms");
            if (elapsed > 20000) break; // stop if taking too long
        }
        return bestMove;
    }

    private Move searchRoot(Board board, String aiColor, boolean isMaximizing, int depth) {
        ArrayList<Move> moves = board.getAllPseudoMoves(aiColor);
        Move bestMove = null;
        float bestScore = isMaximizing ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;

        // Use full window at root — don't tighten alpha/beta between moves
        for (Move m : orderMoves(moves, board)) {
            board.makeMove(m);
            if (board.isKingInCheck(aiColor)) { board.undoMove(m); continue; }

            float val = minimax(board, depth - 1, 
                            Float.NEGATIVE_INFINITY,  // fresh window each move
                            Float.POSITIVE_INFINITY, 
                            !isMaximizing);
            board.undoMove(m);

            if (isMaximizing ? val > bestScore : val < bestScore) {
                bestScore = val;
                bestMove = m;
            }
        }
        return bestMove;
    }

    private float minimax(Board currentState, int depth, float alpha, float beta, boolean maximizing) {
        long hash = currentState.getZobristHash();
        TTEntry cached = transpositionTable.get(hash);
        if (cached != null && cached.depth >= depth) {
            if (cached.flag == TTEntry.EXACT) return cached.score;
            if (cached.flag == TTEntry.LOWER) alpha = Math.max(alpha, cached.score);
            if (cached.flag == TTEntry.UPPER) beta  = Math.min(beta,  cached.score);
            if (beta <= alpha) return cached.score;
        } 

        String currColor = maximizing ? "white" : "black"; // whites perspective, maximizing = white

        
        ArrayList<Move> allTeamMoves = currentState.getAllPseudoMoves(currColor);
        if (allTeamMoves.isEmpty()) {
            return currentState.isKingInCheck(currColor)
                ? (maximizing ? -1.0f : 1.0f)
                : 0.0f;
        }

        // Leaf node evaluation
        if (depth == 0) {
            //float score = nnEvaluator.evaluate(currentState);
            float score = hybridEvaluate(currentState);
            transpositionTable.put(hash, new TTEntry(score, 0, TTEntry.EXACT, null));
            return score;
        }
        List<Move> ordered = orderMoves(allTeamMoves, currentState);
        if(maximizing) { //Maximizing score
            float bestScore = Float.NEGATIVE_INFINITY;
            int flag = TTEntry.UPPER;
            Move bestMove = null;
            boolean hasMoved = false;
            
            for(Move m : ordered) {
                if(m.castleMove) {
                    if(!currentState.castleCheck(currColor, m)) {
                        continue;
                    }
                }
                currentState.makeMove(m);
                if(currentState.isKingInCheck(currColor)) {
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
                        alpha = score;
                        flag = TTEntry.EXACT;
                    }
                }
                if(beta <= alpha) { 
                    flag = TTEntry.LOWER;
                    break;
                }
            }

            if (!hasMoved) {
                return currentState.isKingInCheck(currColor)
                    ? (maximizing ? -1.0f : 1.0f)
                    : 0.0f;
            }
            transpositionTable.put(hash, new TTEntry(bestScore, depth, flag, bestMove));
            return bestScore;
        }
        else { //Minimizing score
            float lowestScore = Float.POSITIVE_INFINITY;
            int flag = TTEntry.UPPER;
            Move worstMove = null;
            boolean hasMoved = false;

            for(Move m : ordered) {
                if(m.castleMove) {
                    if(!currentState.castleCheck(currColor, m)) {
                        continue;
                    }
                }
                currentState.makeMove(m);
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
                        beta = score;
                        flag = TTEntry.EXACT;
                    }
                }
                if(beta <= alpha) {
                    flag = TTEntry.UPPER;
                    break;
                }
            }
            if (!hasMoved) {
                return currentState.isKingInCheck(currColor)
                    ? (maximizing ? -1.0f : 1.0f)
                    : 0.0f;
            }
            transpositionTable.put(hash, new TTEntry(lowestScore, depth, flag, worstMove));
            return lowestScore;
        }
    } 

    private int pieceValue(Piece piece) {
        if(piece == null || piece instanceof King) { return 0; }
        if(piece instanceof Pawn) { return 1; }
        if(piece instanceof Knight) { return 3; }
        if(piece instanceof Bishop) { return 3; }
        if(piece instanceof Rook) { return 5; }
        if(piece instanceof Queen) { return 9; }
        throw new IllegalArgumentException("Unknown piece type");
    }

    private int mvvLva(Move m) {
        if (m.capturedPiece == null) return 0;
        // Prioritize capturing high value pieces with low value pieces
        return pieceValue(m.capturedPiece) * 10 
            - pieceValue(m.piece);
    }

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