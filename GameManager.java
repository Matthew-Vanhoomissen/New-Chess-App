/**
 * @author Matthew Vanhoomissen
 * @version 1.0.1
 * Game manager class that controlls game operations
 * like current turn and checks if the input is a 
 * valid move
 */
import java.util.ArrayList;

public class GameManager {
   private Board board;
   private String currentTurn;
   private ChessPanel panel;
   private Position selectedPosition = null;
   private ArrayList<Move> legalMoves = new ArrayList<>();
   
   public GameManager(Board board, ChessPanel panel) {
    this.board = board;
    this.panel = panel;
    this.currentTurn = "white";

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
         int gameState = board.checkGameState(currentTurn.equals("white") ? "black" : "white");
         System.out.println(gameState);
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