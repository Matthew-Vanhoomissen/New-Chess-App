package game;

import pieces.*;

public class Move {
    public Piece piece;
    public Piece capturedPiece;
    public Piece enPassantPiece;
    public Piece castleRook;

    public Position enPassantPosition;
    public Position rookPositionStart;
    public Position rookPositionEnd;
    public Position start;
    public Position end;

    public boolean firstMove;
    public boolean enPassantMove;
    public boolean castleMove;

    public Move prevLastMove;
    

    public Move(Piece piece, Position start, Position end, Piece capturedPiece) {
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.capturedPiece = capturedPiece;
        this.enPassantMove = false;
        this.castleMove = false;
    }

    public Move(Piece piece, Position start, Position end, Piece enPassantPiece, Position enPassantPosition) {
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.enPassantPiece = enPassantPiece;
        this.enPassantPosition = enPassantPosition;
        this.enPassantMove = true;
        this.castleMove = false;
    }

    public Move(Piece piece, Position start, Position end, Piece castleRook, Position rookPositionStart, Position rookPositionEnd) {
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.castleRook = castleRook;
        this.rookPositionStart = rookPositionStart;
        this.rookPositionEnd = rookPositionEnd;
        this.castleMove = true;
        this.enPassantMove = false;
    }


    public String toString() {
        return end.row + " " + end.col;
    }
}
