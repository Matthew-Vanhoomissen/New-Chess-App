package ml;

import game.*;
import pieces.*;
import transposition.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

public class ChessEvaluator {
    private MultiLayerNetwork model;
    private final INDArray reusableInput = Nd4j.create(1, 781);
    //private HashMap<Long, TTEntry> transpositionTable = new HashMap<>();

    public ChessEvaluator(MultiLayerNetwork model) {
        this.model = model;
    }

    public float evaluate(Board board) {
        float[] input = BoardEncoder.convertBoard(board);
        reusableInput.assign(Nd4j.create(input).reshape(1, 781));
        return model.output(reusableInput).getFloat(0); // single value between -1 and 1
    }

    public float simpleEvaluate(Board board) {
        float white = 0, black = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = board.pieceThere(i, j);
                if (p == null) continue;

                boolean isWhite = p.color.equals("white");

                // 1 - Material
                float material = SemiRandom.pieceValue(p);

                // 2 - Mobility (number of moves available)
                float mobility = board.getLegalMoves(new Position(i, j)).size() * 0.1f;

                // 3 - Pawn structure
                float pawnBonus = 0;
                if (p instanceof Pawn) {
                    // Bonus for being closer to promotion
                    pawnBonus += isWhite ? (6 - i) * 0.05f : i * 0.05f;
                    // Penalty for doubled pawns (another pawn on same file)
                    pawnBonus -= countPawnsOnFile(board, j, p.color) > 1 ? 0.3f : 0;
                }

                // 4 - King safety (penalty for exposed king)
                float kingSafety = 0;
                if (p instanceof King) {
                    kingSafety -= board.getLegalMoves(new Position(i, j)).size() * 0.2f;
                    kingSafety += p.hasMoved ? -0.5f : 0; // penalty if king has moved (lost castling)
                }

                // 5 - Center control bonus for knights/bishops
                float centerBonus = 0;
                if (p instanceof Knight || p instanceof Bishop) {
                    boolean nearCenter = (i >= 2 && i <= 5 && j >= 2 && j <= 5);
                    centerBonus += nearCenter ? 0.2f : 0;
                }

                float total = material + mobility + pawnBonus + kingSafety + centerBonus;
                if (isWhite) white += total;
                else         black += total;
            }
        }

        return (white - black) / 39.0f; // normalize to roughly -1 to 1
    }

    private int countPawnsOnFile(Board board, int j, String color) {
        int counter = 0;
        for(int i = 0; i < 8; i++) {
            Piece piece = board.pieceThere(i, j);
            if(piece != null && piece instanceof Pawn && piece.color.equals(color)) {
                counter++;
            }
        }
        return counter;
    }

    public void save(String path) throws IOException {
        ModelSerializer.writeModel(model, path, true);
    }

    public static ChessEvaluator load(String path) throws IOException {
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(path);
        return new ChessEvaluator(model);
    }

    public Move getAIMove(Board board, String aiColor) {
        //transpositionTable.clear();
        ArrayList<Move> allTeamMoves = board.getAllTeamMoves(aiColor);
        Move bestMove = null;
        float bestScore = (aiColor.equals("white") ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY);
        for(Move m : allTeamMoves) {
            board.makeMove(m);
            float val = minimax(board, 4, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, aiColor.equals("white"));
            board.undoMove(m);
            if(aiColor.equals("white")) {
                if(val > bestScore) {
                    bestScore = val;
                    bestMove = m;
                }
            }
            else {
                if(val < bestScore) {
                    bestScore = val;
                    bestMove = m;
                }
            }
        }
        return bestMove;
    }

    private float minimax(Board currentState, int depth, float alpha, float beta, boolean maximizing) {
        /*long hash = ZobristHash.compute(currentState);
        TTEntry cached = transpositionTable.get(hash);
        if (cached != null && cached.depth >= depth) {
            if (cached.flag == TTEntry.EXACT) return cached.score;
            if (cached.flag == TTEntry.LOWER) alpha = Math.max(alpha, cached.score);
            if (cached.flag == TTEntry.UPPER) beta  = Math.min(beta,  cached.score);
            if (beta <= alpha) return cached.score;
        } */

        String currColor = maximizing ? "white" : "black"; // whites perspective, maximizing = white
        
        ArrayList<Move> allTeamMoves = currentState.getAllTeamMoves(currColor);
        if(depth == 0 || allTeamMoves.isEmpty()) { //At end of the tree
            float score = this.simpleEvaluate(currentState);
            //transpositionTable.put(hash, new TTEntry(score, 0, TTEntry.EXACT));
            return score; //The NN board score go here;
        }
        List<Move> ordered = orderMoves(allTeamMoves);
        if(maximizing) { //Maximizing score
            float bestScore = Float.NEGATIVE_INFINITY;
            //int flag = TTEntry.UPPER;
            
            for(Move m : ordered) {
                currentState.makeMove(m);
                float score = minimax(currentState, depth - 1, alpha, beta, !maximizing);
                currentState.undoMove(m);
                if (score > bestScore) {
                    bestScore = score;
                    if (score > alpha) {
                        alpha = score;
                        //flag = TTEntry.EXACT;
                    }
                }
                if(beta <= alpha) { 
                    //flag = TTEntry.LOWER;
                    break;
                }
            }
            //transpositionTable.put(hash, new TTEntry(bestScore, depth, flag));
            return bestScore;
        }
        else { //Minimizing score
            float lowestScore = Float.POSITIVE_INFINITY;
            //int flag = TTEntry.UPPER;
            
            for(Move m : ordered) {
                currentState.makeMove(m);
                float score = minimax(currentState, depth - 1, alpha, beta, !maximizing);
                currentState.undoMove(m);
                if (score < lowestScore) {
                    lowestScore = score;
                    if (score < beta) {
                        beta = score;
                        //flag = TTEntry.EXACT;
                    }
                }
                if(beta <= alpha) {
                    //flag = TTEntry.LOWER;
                    break;
                }
            }
            //transpositionTable.put(hash, new TTEntry(lowestScore, depth, flag));
            return lowestScore;
        }
    } 

    private List<Move> orderMoves(ArrayList<Move> moves) {
        moves.sort((a, b) -> {
            return SemiRandom.pieceValue(b.capturedPiece) - SemiRandom.pieceValue(a.capturedPiece);
        });
        return moves;
    }
}