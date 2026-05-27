package run;


import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import game.*;
import ml.SimulateGame;
import ml.TrainingDataGen;
import ml.TrainingDataGen.Sample;
import ui.*;

public class Main {
    /*public static void main(String[] args) {
        Board board = new Board();
        board.createBoard();

        JFrame frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ChessPanel panel = new ChessPanel(board);
        GameManager manager = new GameManager(board, panel);
        panel.setManager(manager);
        
        frame.add(panel);

        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    } */

    public static void main(String[] args) {
        List<TrainingDataGen.Sample> test = SimulateGame.generateGames(5000, 200);
        float sum = 0, min = Float.MAX_VALUE, max = Float.MIN_VALUE;
        int nearZero = 0;

        for (Sample s : test) {
            sum += s.label;
            min = Math.min(min, s.label);
            max = Math.max(max, s.label);
            if (Math.abs(s.label) < 0.05f) nearZero++;
        }

        System.out.println("Mean: " + sum / test.size());
        System.out.println("Min: " + min + " Max: " + max);
        System.out.println("Near zero: " + nearZero + "/" + test.size());
    }
}