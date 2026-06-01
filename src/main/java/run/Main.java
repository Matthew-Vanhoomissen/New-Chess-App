package run;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.bytedeco.videoinput.videoDevice;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import game.*;
import ml.*;
import ui.*;
import parser.*;

public class Main {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartWindow();
        });
    }
    
    /*
    public static void main(String[] args) {
        
        
        try {
            List<TrainingDataGen.Sample> samples = PGNParser.parsePGN("Carlsen.pgn");
            Collections.shuffle(samples);
            ModelTrainer.train(samples);
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        } 
        Board testing = new Board();
        testing.createBoard();
        float[] input = BoardEncoder.convertBoard(testing);
        for(int i = 0; i < 781; i++) {
            if(i % 8 == 0) {
                System.out.println("");
            }
            if(i % 64 == 0) {
                System.out.println("");
            }
            System.out.print(input[i] + " ");

        }
    } 
    */
}