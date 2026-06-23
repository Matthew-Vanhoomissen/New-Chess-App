package parser;

/**
 * Utilizes ChessLib library to parse PGN (chess game files) into 
 * compatible moves to simulate games and populate the training and testing
 * sample list
 * 
 * @author Matthew-Vanhoomissen
 */

import com.github.bhlangonijr.chesslib.pgn.PgnHolder;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.MoveList;

import ml.*;
import game.*;
import pieces.*;
import java.util.*;

public class PGNParser {

    /**
     * Loads and parses chess file format PGN which contains player information and more
     * importantly the moves in order of a chess game. 
     * 
     * This method obtains the program compatible moves through the ChessLib PgnHolder 
     * and Game class methods. It saves each board state and assigns the label or result 
     * to which ever side won that game (white = 1, black = -1, stalemate = 0). These samples
     * are then used to train and test the model via {@link ml.ModelTrainer#train(List)}
     * 
     * @param pgnPath pgn file
     * @return sample ArrayList
     * @throws Exception error reading game
     */
    public static List<TrainingDataGen.Sample> parsePGN(String pgnPath) throws Exception {
        int games = 0;
        StockfishEvaluator sf = new StockfishEvaluator("stockfish.exe");
        List<TrainingDataGen.Sample> samples = new ArrayList<>();

        PgnHolder pgn = new PgnHolder(pgnPath);
        pgn.loadPgn(); //Convert

        //Iterate through each game to populate the ArrayList
        for (Game game : pgn.getGames()) {
            try {
                games++;
                game.loadMoveText();
                MoveList moves = game.getHalfMoves();

                //TODO instead of iterating through each game, just iterate through each move so this can work

                List<TrainingDataGen.Sample> gameSamples = processGame(moves, sf);
                samples.addAll(gameSamples);
                if(games % 100 == 0) {
                    System.out.println("Games generated: " + games);
                    System.out.println("Samples: " + samples.size());
                    if(games == 100000) {
                        return samples;
                    }
                }
                
            } catch (Exception e) {
                // Skip malformed games
                System.out.println("Issue");
                continue;
            }
        }
        System.out.println("Parsed " + samples.size() + " samples");
        return samples;
    }

    /**
     * Iterates through each move and converts to program compatible move. If successful,
     * it creates a sample with board state and input label. Finally makes move on board
     * to progress game as in move list.
     * 
     * @param moves in game
     * @param label result of the game (already parsed)
     * @return samples from this specific game
     */
    private static List<TrainingDataGen.Sample> processGame(MoveList moves, StockfishEvaluator sf) {
        List<TrainingDataGen.Sample> samples = new ArrayList<>();
        Board board = new Board();
        board.createBoard();

        for (com.github.bhlangonijr.chesslib.move.Move chessMove : moves) {
            try {
                // Record position before move
                float label = sf.evaluate(board.toFEN(), 6);
                samples.add(new TrainingDataGen.Sample(
                    BoardEncoder.convertBoard(board), label));

                // Translate ChessLib move to program move
                Move move = translateMove(board, chessMove);
                if (move == null) {
                    break; // couldn't translate, skip rest of game
                }
                board.makeMove(move);
            }
            catch(Exception e){
                break;
            }
        }
        return samples;
    }

    /**
     * Converts move from ChessLib into program move {@link game.Move}. Accounts
     * for special move types and positional information.
     * 
     * @param board current state
     * @param chessMove ChessLib move
     * @return program move
     */
    private static Move translateMove(Board board, com.github.bhlangonijr.chesslib.move.Move chessMove) {
        String from = chessMove.getFrom().value(); // Chess notation like e2 a5 etc
        String to   = chessMove.getTo().value();

        Position fromPos = algebraicToPosition(from); //Convert to program position with rows and columns
        Position toPos   = algebraicToPosition(to);



        Piece piece = board.pieceThere(fromPos.row, fromPos.col); //Moved piece
        if (piece == null) return null;

        // Castling — king moves two squares
        if (piece instanceof King && Math.abs(fromPos.col - toPos.col) == 2) {
            boolean kingSide = toPos.col > fromPos.col;

            // Rook positions depend on side
            int rookStartCol = kingSide ? 7 : 0;
            int rookEndCol   = kingSide ? 5 : 3;

            Position rookStart = new Position(fromPos.row, rookStartCol);
            Position rookEnd   = new Position(fromPos.row, rookEndCol);
            Piece rook = board.pieceThere(rookStart.row, rookStart.col);

            if (rook == null) return null;
            return new Move(piece, fromPos, toPos, rook, rookStart, rookEnd);
        }

        Piece captured = board.pieceThere(toPos.row, toPos.col);

        // En passant — pawn moves diagonal to empty square
        if (piece instanceof Pawn && captured == null 
                && fromPos.col != toPos.col) {
            Move move = new Move(piece, fromPos, toPos, null);
            move.enPassantMove = true;
            move.enPassantPosition = new Position(fromPos.row, toPos.col);
            move.enPassantPiece = board.pieceThere(fromPos.row, toPos.col);
            return move;
        }

        // Promotion — pawn reaches back rank
        if (piece instanceof Pawn && (toPos.row == 0 || toPos.row == 7)) {
            // ChessLib tells you what piece it promotes to
            String promoted = chessMove.getPromotion().value();
            Move move = new Move(piece, fromPos, toPos, captured);
            move.promotionType = promoted.toLowerCase();
            return move;
        }

        // Standard move
        return new Move(piece, fromPos, toPos, captured);
    }

    // "e2" -> Position(6, 4) in your row/col format
    private static Position algebraicToPosition(String square) {
        int col = square.charAt(0) - 'A';         // a=0, b=1, ... h=7
        int row = 8 - (square.charAt(1) - '0');   // 1=row7, 8=row0
        return new Position(row, col);
    }
}