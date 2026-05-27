package run;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import game.*;
import ml.*;
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
        List<TrainingDataGen.Sample> samples = SimulateGame.generateGames(10000, 200);
        Collections.shuffle(samples);
        List<TrainingDataGen.Sample> subset = samples.subList(0, 500000);
        
        try {
            ModelTrainer.train(subset);
        }
        catch(IOException e) {
            System.out.println(e.getMessage());
        }
        
        
    }
}