package game;

import java.util.ArrayList;
import java.util.Stack;

import pieces.*;
import transposition.ZobristHash;

public class Board {
    public Piece[][] pieces;
    public Stack<Move> prevMoves;
    public Move prevMove;
    private Position whiteKing;
    private Position blackKing;
    private long zobristHash = 0L;


    public Board() {
        pieces = new Piece[8][8];
        prevMoves = new Stack<>();
        initZobrist();
    }

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

    public ArrayList<Move> getLegalMoves(Position pos) {
        Piece selectedPiece = pieceThere(pos.row, pos.col);
        ArrayList<Move> legalMoves = selectedPiece.getPseudoLegalMoves(this, pos);
        for(int i = legalMoves.size() - 1; i >= 0; i--) {
            Move move = legalMoves.get(i);
            if(move.castleMove) {
                if(!castleCheck(move.piece.color, move)) {
                    legalMoves.remove(i);
                }
            }
            else {
                makeMove(move);
                boolean inCheck = isKingInCheck(selectedPiece.color);
                undoMove(move); 
                if (inCheck || move.capturedPiece instanceof King) {
                    legalMoves.remove(i);
                }
            }
            
        }
        return legalMoves;
    }

    public ArrayList<Move> getAllTeamMoves(String team) {
        ArrayList<Move> totalMoves = new ArrayList<>();
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                Piece piece = pieceThere(i, j);
                if(piece != null && piece.color.equals(team)) {
                    totalMoves.addAll(getLegalMoves(new Position(i, j)));
                }
            }
        }
        return totalMoves;
    }

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

    public int checkGameState(String color) {
        if(!hasMovesLeft(color)) {
            return (isKingInCheck(color) ? 1 : 2);
        }
        return 0;        
    }

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


    public boolean isKingInCheck(String color) {
        Position king = (color.equals("white") ? whiteKing : blackKing);
        int kingRow = king.row;
        int kingCol = king.col;

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
        //Pawns
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

        //Bishop/Queen
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

        //Rook/Queen
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

        //King
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

    public void makeMove(Move move) {
        zobristHash ^= ZobristHash.get(move.piece, move.end.row * 8 + move.end.col);
        if (move.capturedPiece != null)
            zobristHash ^= ZobristHash.get(move.capturedPiece, move.end.row * 8 + move.end.col);
        zobristHash ^= ZobristHash.get(move.piece, move.start.row * 8 + move.start.col);
        zobristHash ^= ZobristHash.getBlackToMove();

        move.prevLastMove = this.prevMove;
        Piece movedPiece = move.piece;

        move.capturedPiece = pieces[move.end.row][move.end.col];

        pieces[move.start.row][move.start.col] = null;

        pieces[move.end.row][move.end.col] = movedPiece;

        if(movedPiece instanceof King) {
            Position king = (movedPiece.color.equals("white") ? whiteKing : blackKing);
            king.row = move.end.row;
            king.col = move.end.col;
        }

        if(move.enPassantMove) {
            pieces[move.enPassantPosition.row][move.enPassantPosition.col] = null;
        }
        else if(move.castleMove) {
            pieces[move.rookPositionStart.row][move.rookPositionStart.col] = null;
            pieces[move.rookPositionEnd.row][move.rookPositionEnd.col] = move.castleRook;
            move.castleRook.setMoved(true);
        }

        move.firstMove = movedPiece.hasMoved;
        movedPiece.setMoved(true);
        this.prevMove = move;
    }

    public void undoMove(Move move) {
        zobristHash ^= ZobristHash.get(move.piece, move.end.row * 8 + move.end.col);
        if (move.capturedPiece != null)
            zobristHash ^= ZobristHash.get(move.capturedPiece, move.end.row * 8 + move.end.col);
        zobristHash ^= ZobristHash.get(move.piece, move.start.row * 8 + move.start.col);
        zobristHash ^= ZobristHash.getBlackToMove();

        Piece movedPiece = move.piece;

        pieces[move.start.row][move.start.col] = move.piece;
        pieces[move.end.row][move.end.col] = move.capturedPiece;

        if(movedPiece instanceof King) {
            Position king = (movedPiece.color.equals("white") ? whiteKing : blackKing);
            king.row = move.start.row;
            king.col = move.start.col;
        }

        if(move.enPassantMove) {
            pieces[move.enPassantPosition.row][move.enPassantPosition.col] = move.enPassantPiece;
        }
        else if(move.castleMove) {
            pieces[move.rookPositionStart.row][move.rookPositionStart.col] = move.castleRook;
            pieces[move.rookPositionEnd.row][move.rookPositionEnd.col] = null;
            move.castleRook.setMoved(false);
        }
        
        move.piece.setMoved(move.firstMove);
        this.prevMove = move.prevLastMove;
    }

    public void addMove(Move move) {
        prevMoves.add(move);
    }

    public Move getPreviousMove() {
        return prevMove;
    }

    public boolean castleCheck(String color, Move move) {
        if(isKingInCheck(color)) { return false; }

        int direction = (move.end.col == 2 ? -1 : 1);
        int c = move.start.col + direction;

        Move testMove;
        boolean inCheck;
        while(c != move.end.col) {
            testMove = new Move(move.piece, move.start, new Position(move.start.row, c), null);
            makeMove(testMove);
            inCheck = isKingInCheck(color);
            undoMove(testMove);
            if(inCheck) { return false; }
            c += direction;
        }
        testMove = new Move(move.piece, move.start, new Position(move.start.row, c), null);
        makeMove(testMove);
        inCheck = isKingInCheck(color);
        undoMove(testMove);
        return (inCheck ? false : true);
    }

    public boolean kingSideCastle(String color) {
        Position kingPosition = color.equals("white") ? whiteKing : blackKing; 
        Piece king = pieceThere(kingPosition.row, kingPosition.col);
        if(king == null) { return false; }

        if(king.hasMoved) { return false; }
        Piece rook = pieceThere(kingPosition.row, 7);
        if(rook != null && rook instanceof Rook && !rook.hasMoved && rook.color.equals(color)) { return true;}
        return false;
    }

    public boolean queenSideCastle(String color) {
        Position kingPosition = color.equals("white") ? whiteKing : blackKing; 
        Piece king = pieceThere(kingPosition.row, kingPosition.col);
        if(king == null) { return false; }

        if(king.hasMoved) { return false; }
        Piece rook = pieceThere(kingPosition.row, 0);
        if(rook != null && rook instanceof Rook && !rook.hasMoved && rook.color.equals(color)) { return true;}
        return false;
    }

    public long getZobristHash() {
        return zobristHash;
    }

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

    public void switchTurn() {
        zobristHash ^= ZobristHash.getBlackToMove();
    }

}
