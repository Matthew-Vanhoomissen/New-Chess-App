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
   
   /**
    * Constructor to initialize object variables to correct starting value.
    * Can load model from weights or can generate weights from .zip file
    * of the full model.
    * 
    * @param board the manager utilizes
    * @param panel visual rendering
    * @param playerColor starts as white or black depending on input
    */
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
    * Calls different methods depending if there is a selected piece. If 
    * not it selects one. Called from {@link ui.ChessPanel#ChessPanel(Board, int)} 
    * when it detects player click.
    * 
    * @param pos or square that was clicked
    */
   public void handleClick(Position pos) {
      if(firstMove && aiColor.equals("white")) { //Triggers ai move so game starts on click
         firstMove = false;
         modelMove();
         return;
      }
      Piece piece = board.pieceThere(pos.row, pos.col);
      if(selectedPosition == null) { //If no piece is selected
         if(piece != null && piece.color.equals(currentTurn)) { //If correct color was selected, select piece
            selectPiece(pos);
            return;
         }
      }
        
      moveOrReselect(pos, piece); //When selection is not blank
        
   }

   /**
    * Generates the legal moves for that piece to draw on screen. Saves selected 
    * position to track whether it exists
    * 
    * @param pos that was clicked
    */
   public void selectPiece(Position pos) {
      ArrayList<Move> moves = board.getLegalMoves(pos);
      selectedPosition = pos;
      legalMoves = moves;
      panel.setHighlightedMoves(moves);
      panel.repaint();
   }

   /**
    * Will swap selection if it is valid or process a move if it is
    * in the legal moves array. If input not valid, it clears the
    * selected piece
    * 
    * @param pos that was clicked 
    * @param piece possible piece to reselect
    */
   public void moveOrReselect(Position pos, Piece piece) {
      Move move = findMoveTo(pos); //If move is in legal moves array

      if(move != null) { //Valid move was selected so process it
         firstMove = false;
         board.makeMove(move);
         endTurn();
         return;
      }

      if(piece != null && piece.color.equals(currentTurn)) { //Different valid piece was selected
         selectPiece(pos);
         return;
      }

      clearSelection(); //Invalid input so clear selection
   }

   /**
    * Called at the end of every turn to clear selection and swap teams.
    * Will also check for end of game scenarios and properly display result.
    * 
    */
   public void endTurn() {
      changeTurn();
      clearSelection();
      int state = board.checkGameState(currentTurn); //0 if game continues, 1 for checkmate, 2 for stalemate
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

   /**
    * Gets move from the model through {@link ml.ChessEvaluator#getAIMove(Board, String)}
    * and ends turn
    */
   private void modelMove() {
      Move aiMove = model.getAIMove(board, currentTurn);
      board.makeMove(aiMove);
      endTurn();
      System.out.println(currentTurn);
   }

   /**
    * Removes selection and clears the legal moves array and
    * redraws board
    */
   public void clearSelection() {
      legalMoves.clear();
      selectedPosition = null;
      panel.repaint();
   }

   /**
    * Searches through legal move array if the end position is a valid 
    * destination. Returns that move or null if invalid
    * 
    * @param pos end position
    * @return the move or null
    */
   public Move findMoveTo(Position pos) {
      for(Move m : legalMoves) {
         if(m.end.equals(pos)) {
            return m;
         }
      }
      return null;
   }

   /**
    * Swaps turn from one color to the other
    * 
    */
   public void changeTurn() {
      if(currentTurn.equals("white")) {
         this.currentTurn = "black";
      }
      else {
         this.currentTurn = "white";
      }
   }

   /**
    * Gets player color whoch is used for swaping sides
    * in {@link ui.ChessPanel}
    * 
    * @return player color
    */
   public String getPlayerColor() {
      return playerColor;
   }

}