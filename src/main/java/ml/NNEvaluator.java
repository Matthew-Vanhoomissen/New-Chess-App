package ml;

/**
 * Uses reusable buffers and strict float array inputs to simulate forward propogation
 * without using ND4 overhead which can become less time efficient in deep searches
 * 
 * @author Matthew-Vanhoomissen
 */

import game.*;
import java.io.*;

public class NNEvaluator {
    // Layer weights as 2D arrays [output][input]
    private final float[][] w1, w2, w3, w4;
    private final float[]   b1, b2, b3, b4;

    // Reusable buffers — avoids allocation on every call
    private final float[] layer1Out = new float[128];
    private final float[] layer2Out = new float[64];
    private final float[] layer3Out = new float[32];

    public NNEvaluator(String weightsPath) throws IOException {
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(weightsPath)))) {

            // Layer 1: 781 -> 128
            w1 = readMatrix(dis, 128, 781);
            b1 = readArray(dis, 128);
            // Layer 2: 128 -> 64
            w2 = readMatrix(dis, 64, 128);
            b2 = readArray(dis, 64);
            // Layer 3: 64 -> 32
            w3 = readMatrix(dis, 32, 64);
            b3 = readArray(dis, 32);
            // Output: 32 -> 1
            w4 = readMatrix(dis, 1, 32);
            b4 = readArray(dis, 1);
        }
        System.out.println("Weights loaded from " + weightsPath);
    }

    public float evaluate(Board board) {
        float[] input = BoardEncoder.convertBoard(board);

        // Forward pass — reuse buffers, no allocation
        linear(w1, b1, input, layer1Out);
        relu(layer1Out);

        linear(w2, b2, layer1Out, layer2Out);
        relu(layer2Out);

        linear(w3, b3, layer2Out, layer3Out);
        relu(layer3Out);

        float[] out = new float[1];
        linear(w4, b4, layer3Out, out);
        return (float) Math.tanh(out[0]);
    }

    // Matrix multiply + bias into pre-allocated output buffer
    private void linear(float[][] weights, float[] bias, float[] input, float[] output) {
        for (int i = 0; i < weights.length; i++) {
            float sum = bias[i];
            float[] row = weights[i];
            for (int j = 0; j < input.length; j++) {
                sum += row[j] * input[j];
            }
            output[i] = sum;
        }
    }

    // ReLU in place
    private void relu(float[] x) {
        for (int i = 0; i < x.length; i++) {
            if (x[i] < 0) x[i] = 0;
        }
    }

    private float[][] readMatrix(DataInputStream dis, int rows, int cols) throws IOException {
        int len = dis.readInt();
        float[] flat = new float[len];
        for (int i = 0; i < len; i++) flat[i] = dis.readFloat();

        float[][] matrix = new float[rows][cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(flat, i * cols, matrix[i], 0, cols);
        return matrix;
    }

    private float[] readArray(DataInputStream dis, int size) throws IOException {
        int len = dis.readInt();
        float[] arr = new float[len];
        for (int i = 0; i < len; i++) arr[i] = dis.readFloat();
        return arr;
    }
}