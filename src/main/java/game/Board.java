package game;
/**
 * Board class to govern movement, determines valid moves, and 
 * holds piece location
 * 
 * 
 * @author Matthew-Vanhoomissen
 */

import java.util.ArrayList;
import java.util.Stack;

import pieces.*;
import transposition.ZobristHash;

public class Board {
    //Global variables unique to each board object
    public Piece[][] pieces;
    public Move prevMove;
    private Position whiteKing;
    private Position blackKing;
    private long zobristHash = 0L;

    /**
     * Initialize board array and zobristHash
     */
    public Board() {
        pieces = new Piece[8][8];
        initZobrist();
    }

    /**
     * Set pieces on board and track king locations for O(1) lookup
     */
    public void createBoard() {
        String w = "white";
        String b = "black";
        pieces[0][0] = new Rook(b);
        pieces[0][1] = new Knight(b);
        pieces[0][2] = new Bishop(b);
        pieces[0][3] = new Queen(b);
        pieces[0][4] = new King(b);
        pieces[0][5] = new Bishop(b);
        pieces[0][6] = new Knight(b);
        pieces[0][7] = new Rook(b);

        blackKing = new Position(0, 4);

        pieces[7][0] = new Rook(w);
        pieces[7][1] = new Knight(w);
        pieces[7][2] = new Bishop(w);
        pieces[7][3] = new Queen(w);
        pieces[7][4] = new King(w);
        pieces[7][5] = new Bishop(w);
        pieces[7][6] = new Knight(w);
        pieces[7][7] = new Rook(w);

        whiteKing = new Position(7, 4);


        for(int i = 0; i < 8; i++) {
            pieces[1][i] = new Pawn(b);
            pieces[6][i] = new Pawn(w);
        }
        prevMove = null;

    }

    /**
     * Allows O(1) lookup for board pieces
     * 
     * @param row input
     * @param col input
     * @return The piece that is there or null if empty
     */
    public Piece pieceThere(int row, int col) {
        if(row > 7 || row < 0 || col > 7 || col < 0) {
            return null;
        }
        Piece piece = pieces[row][col];
        if(piece == null) {
            return null;
        }
        return piece;
    }

    /**
     * Get the legal moves from the input position. Unlike pseudo-moves, this method
     * checks for attacks on king after move to find if it is valid. If valid it gets added
     * to ArrayList, skips if not.
     * 
     * The pseudo-legal move method is defined here: {@link pieces.Piece#getPseudoLegalMoves(Board, Position)}
     * 
     * @param pos to search from
     * @return ArrayList of legal moves
     */
    public ArrayList<Move> getLegalMoves(Position pos) {
        Piece selectedPiece = pieceThere(pos.row, pos.col); //Find piece at this position
        ArrayList<Move> legalMoves = new ArrayList<>();
        
        if(selectedPiece == null) { return legalMoves; }
         legalMoves = selectedPiece.getPseudoLegalMoves(this, pos); //Get pseudo-legal moves for a piece at this position
        for(int i = legalMoves.size() - 1; i >= 0; i--) {
            Move move = legalMoves.get(i);
            if(move.castleMove) { //Call unique check method for castling since it involves multiple positions
                if(!castleCheck(move.piece.color, move)) {
                    legalMoves.remove(i);
                }
            }
            else {
                makeMove(move); //Simulate move to find if it results in a check
                boolean inCheck = isKingInCheck(selectedPiece.color);
                undoMove(move); 
                if (inCheck || move.capturedPiece instanceof King) { //If in check or can capture king, invalid move
                    legalMoves.remove(i);
                }
            }
            
        }
        return legalMoves;
    }

    /**
     * Given team color, the method iterates through the board and collects the pseudo-legal moves
     * for the piece at the position. 
     * 
     * The pseudo-legal move method is defined here: {@link pieces.Piece#getPseudoLegalMoves(Board, Position)}
     * 
     * @param team of pieces to search for 
     * @return full ArrayList of moves for team
     */
    public ArrayList<Move> getAllPseudoMoves(String team) {
        ArrayList<Move> totalMoves = new ArrayList<>();
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                Piece piece = pieceThere(i, j);
                if(piece != null && piece.color.equals(team)) {
                    totalMoves.addAll(piece.getPseudoLegalMoves(this, new Position(i, j)));
                }
            }
        }
        return totalMoves;
    }

    /**
     * Checks if the input team color has moves to play. If not, game is over
     * and it returns an integer key for checkmate and stalemate.
     * 
     * @param color of team
     * @return game state key
     */
    public int checkGameState(String color) {
        if(!hasMovesLeft(color)) {
            return (isKingInCheck(color) ? 1 : 2);
        }
        return 0;        
    }

    /**
     * Iterates through board to check if there is a possible move. Calls
     * {@link game.Board#getLegalMoves(Position)} for each square and returns
     * if size > 0 for fast time complexity
     * 
     * @param color of team
     * @return whether there is at least 1 possible move
     */
    public boolean hasMovesLeft(String color) {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                Piece piece = pieces[i][j];
                if(piece != null && piece.color.equals(color)) {
                    if(getLegalMoves(new Position(i, j)).size() > 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * From the king position, this goes out to search for enemy pieces
     * in that position that match the attack type. Searches from king instead of
     * opponent piece for faster lookup. The piece is either there or not and does
     * not require computing moves for enemy pieces
     * 
     * @param color of king to search from
     * @return whether at least one enemy piece checks king
     */
    public boolean isKingInCheck(String color) {
        Position king = (color.equals("white") ? whiteKing : blackKing);
        int kingRow = king.row; //Set constants
        int kingCol = king.col;

        //Possible positions for knight
        int[][] offsets = {
            {-2, -1}, {-2,  1},
            {-1, -2}, {-1,  2},
            { 1, -2}, { 1,  2},
            { 2, -1}, { 2,  1}
        };

        for(int[] coor : offsets) {
            int newRow = kingRow + coor[0];
            int newCol = kingCol + coor[1];

            if (newRow < 0 || newRow >= 8 || newCol < 0 || newCol >= 8) {
                continue;
            }

            Piece enemy = pieceThere(newRow, newCol);
            if(enemy != null && !enemy.color.equals(color) && enemy instanceof Knight) { return true; }

        }

        //Possible positions for pawns
        int rowOffset = (color.equals("white") ? -1 : 1);
        int pawnRow = kingRow + rowOffset;
        int pawnCol1 = kingCol - 1; 
        int pawnCol2 = kingCol + 1;
        if(pawnRow >= 0 && pawnRow < 8) {
            Piece enemy;
            if(pawnCol1 >= 0 && pawnCol1 < 8) {
                enemy = pieceThere(pawnRow, pawnCol1);
                if(enemy != null && !enemy.color.equals(color) && enemy instanceof Pawn) { 
                    return true; }
            }
            if(pawnCol2 >= 0 && pawnCol2 < 8) {
                enemy = pieceThere(pawnRow, pawnCol2);
                if(enemy != null && !enemy.color.equals(color) && enemy instanceof Pawn) { 
                    return true; }
            }
        }

        //Possible positions for bishop or diagonals of queen
        int[][] offsetsBishop = {
            {-1, -1}, {-1, 1},
            {1, -1}, {1, 1}
        };
        for(int[] coor : offsetsBishop) {
            int r = coor[0] + kingRow;
            int c = coor[1] + kingCol;
            while(r >= 0 && r < 8 && c >= 0 && c < 8) {
                Piece enemy = pieceThere(r, c);
                if(enemy != null) {
                    if(!enemy.color.equals(color) && (enemy instanceof Bishop || enemy instanceof Queen)) {
                        return true; 
                    }
                    break;
                }
                r += coor[0];
                c += coor[1];
            }
        }

        //Possible positions for rook or lines of queen
        int[][] offsetsRook = {
            {-1, 0}, {1, 0},
            {0, -1}, {0, 1}
        };
        for(int[] coor : offsetsRook) {
            int r = coor[0] + kingRow;
            int c = coor[1] + kingCol;
            while(r >= 0 && r < 8 && c >= 0 && c < 8) {
                Piece enemy = pieceThere(r, c);
                if(enemy != null) {
                    if(!enemy.color.equals(color) && (enemy instanceof Rook || enemy instanceof Queen)) {
                        return true; 
                    }
                    break;
                }
                r += coor[0];
                c += coor[1];
            }
        }

        //Possible positions for king
        int[][] offsetsKing = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {1, -1}, {1, 0}, {1, 1},
            {0, -1}, {0, 1}
        };
        for(int[] coor : offsetsKing) {
            int r = coor[0] + kingRow;
            int c = coor[1] + kingCol;
            if (r < 0 || r >= 8 || c < 0 || c >= 8) {
                continue;
            }
            Piece enemy = pieceThere(r, c);
            if(enemy != null && !enemy.color.equals(color) && enemy instanceof King) {
                return true;
            }

        }
        return false;
    }

    /**
     * Integral method that enacts input move onto the board. This calculates zobristHash to get unique
     * key for board after move. The zobristHash is invertible through XOR which can be undone.
     * Moves selected piece to end location, removes captured piece, and other
     * special moves like castling and en passant. Finally, it stores the before and after for critical 
     * pieces which allows moves to be undone.
     * 
     * @param move to process
     */
    public void makeMove(Move move) {
        //Compute hash
        zobristHash ^= ZobristHash.get(move.piece, move.end.row * 8 + move.end.col);
        if (move.capturedPiece != null)
            zobristHash ^= ZobristHash.get(move.capturedPiece, move.end.row * 8 + move.end.col);
        zobristHash ^= ZobristHash.get(move.piece, move.start.row * 8 + move.start.col);
        zobristHash ^= ZobristHash.getBlackToMove();

        move.prevLastMove = this.prevMove; //Save last board move
        Piece movedPiece = move.piece;

        move.capturedPiece = pieces[move.end.row][move.end.col]; //Save captured piece

        pieces[move.start.row][move.start.col] = null; //remove from old position

        if(!move.promotionType.isEmpty()) { //If there is a promotion, set to desired type
            pieces[move.end.row][move.end.col] = getType(move.promotionType, movedPiece.color);
        }
        else { //Else just move selected piece
            pieces[move.end.row][move.end.col] = movedPiece;
        }
        

        if(movedPiece instanceof King) { //Track king position
            Position king = (movedPiece.color.equals("white") ? whiteKing : blackKing);
            king.row = move.end.row;
            king.col = move.end.col;
        }

        if(move.enPassantMove) { //Remove captured en passant pawn (not the same as captured piece)
            pieces[move.enPassantPosition.row][move.enPassantPosition.col] = null;
        }
        else if(move.castleMove) { //Save old rook location
            pieces[move.rookPositionStart.row][move.rookPositionStart.col] = null;
            pieces[move.rookPositionEnd.row][move.rookPositionEnd.col] = move.castleRook;
            move.castleRook.setMoved(true); //Rook now has moved
        }

        move.firstMove = movedPiece.hasMoved; //Save if the piece has moved before this turn
        movedPiece.setMoved(true); //Piece now has moved
        this.prevMove = move; //Save previous move for O(1) lookup
    }

    /**
     * Integral method that undoes input move from the board and reverts its state.
     * Recomutes the previous zobristHash since it is invertible through XOR. Moves moved
     * piece back to previous position and adds back captured pieces. Finally, it resets 
     * piece values such as hasMoved to the previous value
     * 
     * @param move to undo
     */
    public void undoMove(Move move) {
        //Recalculate zobristHash
        zobristHash ^= ZobristHash.get(move.piece, move.end.row * 8 + move.end.col);
        if (move.capturedPiece != null)
            zobristHash ^= ZobristHash.get(move.capturedPiece, move.end.row * 8 + move.end.col);
        zobristHash ^= ZobristHash.get(move.piece, move.start.row * 8 + move.start.col);
        zobristHash ^= ZobristHash.getBlackToMove();

        Piece movedPiece = move.piece;

        pieces[move.start.row][move.start.col] = move.piece; //Move old pieces back
        pieces[move.end.row][move.end.col] = move.capturedPiece;

        if(movedPiece instanceof King) { //Track king position for O(1) lookup
            Position king = (movedPiece.color.equals("white") ? whiteKing : blackKing);
            king.row = move.start.row;
            king.col = move.start.col;
        }

        if(move.enPassantMove) { //Replace captured en passant pawn
            pieces[move.enPassantPosition.row][move.enPassantPosition.col] = move.enPassantPiece;
        }
        else if(move.castleMove) { //Move old rook back to saved position
            pieces[move.rookPositionStart.row][move.rookPositionStart.col] = move.castleRook;
            pieces[move.rookPositionEnd.row][move.rookPositionEnd.col] = null;
            move.castleRook.setMoved(false);
        }
        
        move.piece.setMoved(move.firstMove); //Reset old value
        this.prevMove = move.prevLastMove; //Reset old previous move
    }

    /**
     * 
     * @return previous move that was process on board
     */
    public Move getPreviousMove() {
        return prevMove;
    }

    /**
     * Unique search for checks when castling since it involves multiple
     * squares. Computes direction from end square (kingside or queenside)
     * and searches for checks in each square the king traverses.
     * 
     * @param color of king
     * @param move that is processed to be valid
     * @return whether the move is valid
     */
    public boolean castleCheck(String color, Move move) {
        if(isKingInCheck(color)) { return false; } //Cannot castle out of check

        int direction = (move.end.col == 2 ? -1 : 1); //Direction king moves
        int c = move.start.col + direction;

        Move testMove;
        boolean inCheck;
        while(c != move.end.col) {
            //Tests if king will be in check by making and undoing move
            testMove = new Move(move.piece, move.start, new Position(move.start.row, c), null);
            makeMove(testMove);
            inCheck = isKingInCheck(color);
            undoMove(testMove);
            if(inCheck) { return false; }
            c += direction;
        }
        //Final check when king is at end position
        testMove = new Move(move.piece, move.start, new Position(move.start.row, c), null);
        makeMove(testMove);
        inCheck = isKingInCheck(color);
        undoMove(testMove);
        return (inCheck ? false : true);
    }

    /**
     * Simple method to check if the possibility of kingside castling is allowed for
     * input team. Does not matter if castling is allowed that move, just that
     * it could be done under right conditions.
     * 
     * This method is used for input to neural network for game state which cannot
     * be deciphered from piece positions
     * 
     * @param color of team
     * @return whether castling could be possible in future
     */
    public boolean kingSideCastle(String color) {
        Position kingPosition = color.equals("white") ? whiteKing : blackKing; 
        Piece king = pieceThere(kingPosition.row, kingPosition.col);
        if(king == null) { return false; } //Ensure king is at position

        if(king.hasMoved) { return false; } //Ensure king hasn't moved
        Piece rook = pieceThere(kingPosition.row, 7);
        //If rook has not moved, castling is possible
        if(rook != null && rook instanceof Rook && !rook.hasMoved && rook.color.equals(color)) { return true;}
        return false;
    }

    /**
     * Simple method to check if the possibility of queenside castling is allowed for
     * input team. Does not matter if castling is allowed that move, just that
     * it could be done under right conditions.
     * 
     * This method is used for input to neural network for game state which cannot
     * be deciphered from piece positions
     * 
     * @param color of team
     * @return whether castling could be possible in future
     */
    public boolean queenSideCastle(String color) {
        Position kingPosition = color.equals("white") ? whiteKing : blackKing; 
        Piece king = pieceThere(kingPosition.row, kingPosition.col);
        if(king == null) { return false; } //Ensure king is at position

        if(king.hasMoved) { return false; } //Ensure king hasn't moved
        Piece rook = pieceThere(kingPosition.row, 0);
        //If rook has not moved, castling is possible
        if(rook != null && rook instanceof Rook && !rook.hasMoved && rook.color.equals(color)) { return true;}
        return false;
    }

    /**
     * Returns the board's current Zobrist hash.
     *
     * The hash is maintained incrementally by {@link #makeMove(Move)} and
     * {@link #undoMove(Move)}, making this an O(1) lookup. Used as the key
     * for transposition table entries during search.
     *
     * @return the 64-bit Zobrist hash representing the current board state
     */
    public long getZobristHash() {
        return zobristHash;
    }

    /**
     * Computes and stores the Zobrist hash from scratch for the current position.
     *
     * Should be called once after the board is initialized via
     * {@link #createBoard()} to establish the baseline hash. After that,
     * {@link #makeMove(Move)} and {@link #undoMove(Move)} maintain the hash
     * incrementally via XOR operations — this method should not be called
     * again during normal play as it is significantly more expensive than
     * the incremental updates.
     */
    public void initZobrist() {
        zobristHash = 0L;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece p = pieceThere(i, j);
                if (p != null)
                    zobristHash ^= ZobristHash.get(p, i * 8 + j);
            }
        }
    }

    /**
     * Flips the side-to-move component of the Zobrist hash.
     *
     * XORs the hash with a dedicated random constant representing black
     * to move, toggling between white-to-move and black-to-move states.
     * Called during null move pruning to skip a side's turn without making
     * a real move — must be called twice (before and after the null search)
     * to restore the original hash, since XOR is its own inverse.
     *
     * @see transposition.ZobristHash#getBlackToMove()
     */
    public void switchTurn() {
        zobristHash ^= ZobristHash.getBlackToMove();
    }


    /**
     * Returns a new piece object for type input
     * 
     * Method is utilized by {@link game.Board#makeMove(Move)} for pawn
     * promotion to a new type
     * 
     * @param type of new object
     * @param color of team
     * @return the new piece object
     */
    private Piece getType(String type, String color) {
        switch (type) {
            case "queen":
                return new Queen(color);
            case "rook":
                return new Rook(color);
            case "knight":
                return new Knight(color);
            case "bishop":
                return new Bishop(color);
        }
        return null;
    }

}
