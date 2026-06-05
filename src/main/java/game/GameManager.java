package game;
/**
 * Game manager class that controlls game operations
 * like current turn end can appropriately conclude the game
 * 
 * 
 * @author Matthew-Vanhoomissen
 */

import java.io.IOException;

import java.util.ArrayList;

import pieces.*;
import ui.*;
import ml.*;

public class GameManager {
   private Board board;
   private String currentTurn;
   private String playerColor;
   private String aiColor;
   private ChessPanel panel;
   private Position selectedPosition = null;
   private ArrayList<Move> legalMoves = new ArrayList<>();
   private ChessEvaluator model;

   private boolean firstMove = true;
   
   public GameManager(Board board, ChessPanel panel, String playerColor) {
      this.board = board;
      this.panel = panel;
      this.currentTurn = "white";
      this.playerColor = playerColor;
      this.aiColor = (playerColor.equals("white") ? "black" : "white");
      try {
         model = ChessEvaluator.loadForRuntime("best_model.bin");
         //model = ChessEvaluator.load("best_model.zip");
         //model.exportWeights("best_model.bin");
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
      if(firstMove && aiColor.equals("white")) {
         firstMove = false;
         modelMove();
         return;
      }
      Piece piece = board.pieceThere(pos.row, pos.col);
      if(selectedPosition == null) {
         if(piece != null && piece.color.equals(currentTurn)) {
            System.out.println(currentTurn + "\n");
            System.out.println(piece.color);
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
         firstMove = false;
         board.makeMove(move);
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
      int state = board.checkGameState(currentTurn);
      if(state != 0) {
         if(state == 1) { // Checkmate
            String winner = currentTurn.equals("white") ? "Black wins" : "White wins";
            panel.showGameOver(winner);
            return;
         }
         else { //Stalemate
            panel.showGameOver("Stalemate");
            return;
         }
      }
      // Trigger AI if it's the AI's turn
      if (currentTurn.equals(aiColor)) {
         modelMove();
      }
   }

   private void modelMove() {
      Move aiMove = model.getAIMove(board, currentTurn);
      board.makeMove(aiMove);
      endTurn();
      System.out.println(currentTurn);
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

   public String getPlayerColor() {
      return playerColor;
   }

}