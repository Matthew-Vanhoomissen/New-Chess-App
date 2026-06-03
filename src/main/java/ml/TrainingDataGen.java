package ml;

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
    }
}
