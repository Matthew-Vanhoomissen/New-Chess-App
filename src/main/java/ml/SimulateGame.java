package ml;

import java.util.ArrayList;
import java.util.List;

import game.*;
import ml.TrainingDataGen.Sample;

public class SimulateGame {
    private static List<TrainingDataGen.Sample> playGame(int maxMoves) {
        Board board = new Board();
        board.createBoard();

        String currTeam = "white";

        List<TrainingDataGen.Sample> samples = new ArrayList<>();
        for(int i = 0; i < maxMoves; i++) {
            float label = SemiRandom.getBoardValue(board);
            samples.add(new Sample(BoardEncoder.convertBoard(board), label));

            Move randomMove = SemiRandom.getRandomMove(board, currTeam);
            if(randomMove == null) {
                break;
            }
            board.makeMove(randomMove);
            currTeam = (currTeam.equals("white") ? "black" : "white");
        }

        return samples;
    }

    public static List<TrainingDataGen.Sample> generateGames(int numGames, int maxMoves) {
        List<TrainingDataGen.Sample> allSamples = new ArrayList<>();
        for (int i = 0; i < numGames; i++) {
            allSamples.addAll(playGame(maxMoves));
            if (i % 100 == 0) System.out.println("Generated " + i + " games...");
        }
        return allSamples;
    }
}
