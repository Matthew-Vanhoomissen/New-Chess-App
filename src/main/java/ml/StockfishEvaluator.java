package ml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Loads the Stockfish engine to assign the board a value which
 * is later utilized by the {@link parser.PGNParser} to create 
 * training data for the neural network to learn position values
 * 
 * @author Matthew-Vanhoomissen
 */

public class StockfishEvaluator {
    private Process stockfish;
    private PrintWriter writer;
    private BufferedReader reader;

    public StockfishEvaluator(String stockfishPath) throws IOException {
        stockfish = new ProcessBuilder(stockfishPath)
            .redirectErrorStream(true)
            .start();
        writer = new PrintWriter(new OutputStreamWriter(stockfish.getOutputStream()), true);
        reader = new BufferedReader(new InputStreamReader(stockfish.getInputStream()));

        // Initialize UCI mode
        writer.println("uci");
        waitFor("uciok");
        writer.println("isready");
        waitFor("readyok");
        System.out.println("Stockfish ready");
    }

    /**
     * Evaluates a position from a FEN string at the given depth.
     * Returns a score normalized to [-1, 1] from white's perspective.
     * Mate scores are clamped to ±1.
     *
     * @param fen   the FEN string of the position to evaluate
     * @param depth the search depth for Stockfish (8-12 recommended)
     * @return normalized evaluation score
     */
    public float evaluate(String fen, int depth) throws IOException {
        writer.println("position fen " + fen);
        writer.println("go depth " + depth);

        String line;
        float score = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("info") && line.contains("depth " + depth)
                    && line.contains("score")) {
                if (line.contains("score cp")) {
                    int idx = line.indexOf("score cp") + 9;
                    int cp = Integer.parseInt(line.substring(idx).trim().split(" ")[0]);
                    // Centipawns to normalized [-1, 1], clamp at 10 pawns
                    score = Math.max(-1f, Math.min(1f, cp / 1000f));
                } else if (line.contains("score mate")) {
                    int idx = line.indexOf("score mate") + 11;
                    int mate = Integer.parseInt(line.substring(idx).trim().split(" ")[0]);
                    score = mate > 0 ? 1.0f : -1.0f;
                }
            }
            if (line.startsWith("bestmove")) break;
        }
        return score;
    }

    public void close() {
        writer.println("quit");
        stockfish.destroy();
    }

    private void waitFor(String token) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(token)) break;
        }
    }
}