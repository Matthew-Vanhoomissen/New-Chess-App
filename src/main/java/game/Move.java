package game;
/**
 * Move class that stores piece moved, captured, positional data, and any
 * other special identifiers for castling or en passant
 * 
 * @author Matthew-Vanhoomissen
 */

import pieces.*;

public class Move {
    public Piece piece;
    public Piece capturedPiece;
    public Piece enPassantPiece; //Captured piece during en passant
    public Piece castleRook;

    public Position enPassantPosition; //Where pawn was captured during en passant
    public Position rookPositionStart; //Castling rook start position
    public Position rookPositionEnd; //Castling rook end position
    public Position start; //Moved piece start
    public Position end; //Moved piece end

    public boolean firstMove;
    public boolean enPassantMove;
    public boolean castleMove;

    public Move prevLastMove;

    public String promotionType = "";
    

    /**
     * Move constructor for ordinary piece moves and captures
     * 
     * @param piece that is moved
     * @param start
     * @param end
     * @param capturedPiece 
     */
    public Move(Piece piece, Position start, Position end, Piece capturedPiece) {
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.capturedPiece = capturedPiece;
        this.enPassantMove = false;
        this.castleMove = false;
    }

    /**
     * Special move constructor for en passant that includes separate captured pawn
     * and where it was captured from.
     * 
     * @param piece
     * @param start
     * @param end
     * @param enPassantPiece
     * @param enPassantPosition
     */
    public Move(Piece piece, Position start, Position end, Piece enPassantPiece, Position enPassantPosition) {
        this.piece = piece;
        this.start = start;
        this.end = end;
        this.enPassantPiece = enPassantPiece;
        this.enPassantPosition = enPassantPosition;
        this.enPassantMove = true;
        this.castleMove = false;
    }

    /**
     * Special move constructor for castling that includes the rook that is
     * moved during castling and its start and end position.
     * 
     * @param piece
     * @param start
     * @param end
     * @param castleRook
     * @param rookPositionStart
     * @param rookPositionEnd
     */
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


    /**
     * Converts move to readable string
     * 
     * @return string
     */
    public String toString() {
        return end.row + " " + end.col;
    }
}
