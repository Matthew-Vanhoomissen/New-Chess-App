package search;

import java.util.ArrayList;

import game.*;

public class AlphaBeta {
    public static float minimax(Board currentState, int depth, float alpha, float beta, boolean maximizing) {
        String currColor = maximizing ? "white" : "black"; // whites perspective, maximizing = white
        
        ArrayList<Move> allTeamMoves = currentState.getAllTeamMoves(currColor);
        if(depth == 0 || allTeamMoves.isEmpty()) { //At end of the tree
            return 0; //The NN board score go here;
        }

        if(maximizing) { //Maximizing score
            float bestScore = Float.NEGATIVE_INFINITY;
            
            for(Move m : allTeamMoves) {
                currentState.makeMove(m);
                float score = minimax(currentState, depth - 1, alpha, beta, !maximizing);
                bestScore = Math.max(bestScore, score);
                alpha = Math.max(alpha, score);
                if(beta <= alpha) { 
                    currentState.undoMove(m);
                    break;
                }
                currentState.undoMove(m);
            }
            return bestScore;
        }
        else { //Minimizing score
            float lowestScore = Float.POSITIVE_INFINITY;
            
            for(Move m : allTeamMoves) {
                currentState.makeMove(m);
                float score = minimax(currentState, depth - 1, alpha, beta, !maximizing);
                lowestScore = Math.min(lowestScore, score);
                beta = Math.min(beta, score);
                if(beta <= alpha) {
                    currentState.undoMove(m);
                    break;
                }
                currentState.undoMove(m);
            }
            return lowestScore;
        }
    } 
}
