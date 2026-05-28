package game;
import java.io.IOException;
/**
 * @author Matthew Vanhoomissen
 * @version 1.0.1
 * Game manager class that controlls game operations
 * like current turn and checks if the input is a 
 * valid move
 */
import java.util.ArrayList;

import pieces.*;
import ui.*;
import ml.*;

public class GameManager {
   private Board board;
   private String currentTurn;
   private ChessPanel panel;
   private Position selectedPosition = null;
   private ArrayList<Move> legalMoves = new ArrayList<>();
   private ChessEvaluator model;
   
   public GameManager(Board board, ChessPanel panel) {
      this.board = board;
      this.panel = panel;
      this.currentTurn = "white";
      try {
         model = ChessEvaluator.loadForRuntime("chess_weights.bin");
         //model = ChessEvaluator.load("chess_model.zip");
         //model.exportWeights("chess_weights.bin");
      }
      catch(IOException e) {
         System.out.println(e.getMessage());
      }
   }

   /**
    * 
    * @param pos
    * Calls different methods depending if
    * there is a selected piece. If not it
    * selects one
    */
   public void handleClick(Position pos) {
        Piece piece = board.pieceThere(pos.row, pos.col);
        if(selectedPosition == null) {
            if(piece != null && piece.color.equals(currentTurn)) {
               selectPiece(pos);
               return;
            }
        }
        
        moveOrReselect(pos, piece);
        
   }

   public void selectPiece(Position pos) {
      ArrayList<Move> moves = board.getLegalMoves(pos);
      selectedPosition = pos;
      legalMoves = moves;
      panel.setHighlightedMoves(moves);
      panel.repaint();
   }

   public void moveOrReselect(Position pos, Piece piece) {
      Move move = findMoveTo(pos);

      if(move != null) {
         board.makeMove(move);
         board.addMove(move);
         int gameState = board.checkGameState(currentTurn.equals("white") ? "black" : "white");
         if(gameState != 0) {
            System.out.println(gameState == 1 ? "Checkmate!" : "Stalemate");
            clearSelection();
            return;
         }
         endTurn();
         return;
      }

      if(piece != null && piece.color.equals(currentTurn)) {
         selectPiece(pos);
         return;
      }

      clearSelection();
   }

   public void endTurn() {
      changeTurn();
      clearSelection();
      modelMove();
   }

   private void modelMove() {
      Move aiMove = model.getAIMove(board, currentTurn);
      board.makeMove(aiMove);
      board.addMove(aiMove);
      int gameState = board.checkGameState(currentTurn.equals("white") ? "black" : "white");
      if(gameState != 0) {
         System.out.println(gameState == 1 ? "Checkmate!" : "Stalemate");
         clearSelection();
         return;
      }
      changeTurn();
      clearSelection();
   }

   public void clearSelection() {
      legalMoves.clear();
      selectedPosition = null;
      panel.repaint();
   }

   public Move findMoveTo(Position pos) {
      for(Move m : legalMoves) {
         if(m.end.equals(pos)) {
            return m;
         }
      }
      return null;
   }

   public void changeTurn() {
      if(currentTurn.equals("white")) {
         this.currentTurn = "black";
      }
      else {
         this.currentTurn = "white";
      }
   }

}