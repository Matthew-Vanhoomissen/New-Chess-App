package ml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Sample object is used for training with the float array input and label which is 
 * expected result
 * 
 * @author Matthew-Vanhoomissen
 */

public class TrainingDataGen {
    // One training sample
    public static class Sample {
        public float[] input;   // 781 board encoding
        public float label;     // 1.0 white won, -1.0 black won, 0.0 draw

        public Sample(float[] input, float label) {
            this.input = input;
            this.label = label;
        }

        public static String inputToString(float[] inp) {
            StringBuilder sb = new StringBuilder();
            for(float f : inp) {
                sb.append(f);
                sb.append(" ");
            }
            return sb.toString();
        }
    }

    public static void toFile(List<Sample> samples) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("samples.txt", true));
            for(Sample s : samples) {
                writer.write(Sample.inputToString(s.input) + "" + s.label);
                writer.newLine();
            }
        }
        catch(IOException e) {

        }
    }
}
