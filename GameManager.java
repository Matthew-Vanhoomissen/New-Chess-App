public class GameManager {
   private Board board;
   private String currentTurn;
   private ChessPanel panel;
   
   public GameManager(Board board, ChessPanel panel) {
    this.board = board;
    this.panel = panel;
    this.currentTurn = "white";
   }

   public void handleClick(Position pos) {
        Piece piece = board.pieceThere(pos.row, pos.col);
        if(piece != null) {
            System.out.println(piece.getPseudoLegalMoves(board, pos));
        }
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