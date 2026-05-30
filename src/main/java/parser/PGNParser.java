package parser;

import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.MoveList;

import ml.*;
import game.*;
import pieces.*;
import java.util.*;

public class PGNParser {

    public static List<TrainingDataGen.Sample> parsePGN(String pgnPath) throws Exception {
        List<TrainingDataGen.Sample> samples = new ArrayList<>();

        PgnHolder pgn = new PgnHolder(pgnPath);
        pgn.loadPgn();

        for (Game game : pgn.getGames()) {
            try {
                game.loadMoveText();
                MoveList moves = game.getHalfMoves();
                String result  = game.getResult().getDescription();

                float label;
                if (result.equals("1-0"))       label = 1.0f;
                else if (result.equals("0-1"))  label = -1.0f;
                else                            label = 0.0f;

                List<TrainingDataGen.Sample> gameSamples = processGame(moves, label);
                samples.addAll(gameSamples);
            } catch (Exception e) {
                // Skip malformed games
                continue;
            }
        }
        System.out.println("Parsed " + samples.size() + " samples");
        return samples;
    }

    private static List<TrainingDataGen.Sample> processGame(MoveList moves, float label) {
        List<TrainingDataGen.Sample> samples = new ArrayList<>();
        Board board = new Board();
        board.createBoard();

        for (com.github.bhlangonijr.chesslib.move.Move chessMove : moves) {
            // Record position before move
            samples.add(new TrainingDataGen.Sample(
                BoardEncoder.convertBoard(board), label));

            // Translate ChessLib move to your Move
            Move move = translateMove(board, chessMove);
            if (move == null) break; // couldn't translate, skip rest of game
            board.makeMove(move);
        }
        return samples;
    }

    private static Move translateMove(Board board, com.github.bhlangonijr.chesslib.move.Move chessMove) {
        // ChessLib uses algebraic square names like "e2", "e4"
        String from = chessMove.getFrom().value(); // e.g. "e2"
        String to   = chessMove.getTo().value();   // e.g. "e4"

        Position fromPos = algebraicToPosition(from);
        Position toPos   = algebraicToPosition(to);

        Piece piece = board.pieceThere(fromPos.row, fromPos.col);
        if (piece == null) return null;

        Piece captured = board.pieceThere(toPos.row, toPos.col);

        // Check en passant
        Move move = new Move(piece, fromPos, toPos, captured);
        if (piece instanceof Pawn && captured == null && fromPos.col != toPos.col) {
            // Pawn moved diagonally without capture = en passant
            move.enPassantMove = true;
            move.enPassantPosition = new Position(fromPos.row, toPos.col);
            move.enPassantPiece = board.pieceThere(fromPos.row, toPos.col);
            move.capturedPiece = null;
        }

        return move;
    }

    // "e2" -> Position(6, 4) in your row/col format
    private static Position algebraicToPosition(String square) {
        int col = square.charAt(0) - 'a';         // a=0, b=1, ... h=7
        int row = 8 - (square.charAt(1) - '0');   // 1=row7, 8=row0
        return new Position(row, col);
    }
}