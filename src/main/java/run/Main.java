package run;
/**
 * Main class to prompt start screen
 * 
 * 
 * @author Matthew-Vanhoomissen
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import ml.ModelTrainer;
import ml.TrainingDataGen;
import parser.PGNParser;
import ui.*;

public class Main {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StartWindow();
        });
    } 
    /* 
    public static void main(String[] args) {
        try{
            List<TrainingDataGen.Sample> samples = PGNParser.parsePGN("Carlsen.pgn");
            TrainingDataGen.toFile(samples);
            ModelTrainer.train(samples);
        }
        catch(Exception e) {

        }
    } */

}