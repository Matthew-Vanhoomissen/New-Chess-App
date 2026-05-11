import java.util.ArrayList;
import java.util.Stack;

public class Board {
    public Piece[][] pieces;
    public Stack<Move> prevMoves;
    private Position whiteKing;
    private Position blackKing;


    public Board() {
        pieces = new Piece[8][8];
        prevMoves = new Stack<>();
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

        blackKing = new Position(7, 4);


        for(int i = 0; i < 8; i++) {
            pieces[1][i] = new Pawn(b);
            pieces[6][i] = new Pawn(w);
        } 

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
            makeMove(legalMoves.get(i));

        }
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

            Piece enemy = pieceThere(kingRow, kingCol);
            if(!enemy.color.equals(color) && enemy instanceof Knight) { return true; }

        }
        

    }

    public Position findKing(String color) {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                Piece piece = pieceThere(i, j);
                if(piece instanceof King && piece.color.equals(color)) {
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    public void makeMove(Move move) {
        Piece movedPiece = move.piece;

        move.capturedPiece = pieces[move.end.row][move.end.col];

        pieces[move.start.row][move.start.col] = null;

        pieces[move.end.row][move.end.col] = movedPiece;

        if(movedPiece instanceof King) {
            Position king = (movedPiece.color.equals("white") ? whiteKing : blackKing);
            king.row = move.end.row;
            king.col = move.end.col;
        }

        move.firstMove = movedPiece.hasMoved;
        movedPiece.setMoved(true);
        prevMoves.add(move);
    }

    public void undoMove(Move move) {
        Piece movedPiece = move.piece;

        if(movedPiece instanceof King) {
            Position king = (movedPiece.color.equals("white") ? whiteKing : blackKing);
            king.row = move.start.row;
            king.col = move.start.col;
        }

        pieces[move.start.row][move.start.col] = move.piece;
        pieces[move.end.row][move.end.row] = move.capturedPiece;

        if(movedPiece instanceof King) {
            Position king = (movedPiece.color.equals("white") ? whiteKing : blackKing);
            king.row = move.end.row;
            king.col = move.end.col;
        }

        move.piece.setMoved(move.firstMove);
    }

}
