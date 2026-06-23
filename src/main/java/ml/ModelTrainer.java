package ml;

/**
 * Creates the layers and nodes for the neural network and in training 
 * effectively measures loss and adjusts weights and biases. 
 * 
 * @author Matthew-Vanhoomissen
 */

import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.DataSet;

import java.util.*;
import java.io.*;

public class ModelTrainer {

    /**
     * Builds the fully connected model with an input layer, 
     * 3 hidden layers, and the output node. Uses the activation
     * function ReLu for forward propogation and Tanh for output (-1 -> 1).
     * 
     * @return model object
     */
    public static MultiLayerNetwork buildModel() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
            .updater(new Adam(0.0001))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(781).nOut(128)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(128).nOut(64)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(64).nOut(32)
                .activation(Activation.RELU)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(32).nOut(1)
                .activation(Activation.TANH)
                .build())
            .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        return model;
    }

    /**
     * Trains the model through the input randomized sample dataset {@link ml.TrainingDataGen.Sample}
     * that is split into training and testing data. The data is input into the model through forward
     * propogation, the training loss is calculated and the model is modified through back propogation. 
     * The training loss is displayed through each sample batch and every epoch. The most accurate 
     * model is saved each epoch and will end if improvement plateaus.
     * 
     * @param samples randomized data
     * @throws IOException
     */
    public static void train(List<TrainingDataGen.Sample> samples) throws IOException {
        // Center labels
        float mean = 0;
        for (TrainingDataGen.Sample s : samples) mean += s.label;
        mean /= samples.size();
        for (TrainingDataGen.Sample s : samples) 
            s.label = Math.max(-1.0f, Math.min(1.0f, s.label - mean));

        //80% of data is training, 20% is testing
        int splitIndex = (int)(samples.size() * 0.8);
        List<TrainingDataGen.Sample> trainSamples = samples.subList(0, splitIndex);
        List<TrainingDataGen.Sample> valSamples   = samples.subList(splitIndex, samples.size());

        int n = trainSamples.size();
        int batchSize = 32;

        //Transfer data into 2D input and label (result) arrays
        float[][] inputs = new float[n][781];
        float[][] labels = new float[n][1];
        for (int i = 0; i < n; i++) {
            inputs[i] = trainSamples.get(i).input;
            labels[i][0] = trainSamples.get(i).label;
        }

        //Testing valuation arrays — built once
        float[][] valInputs = new float[valSamples.size()][781];
        float[][] valLabels = new float[valSamples.size()][1];
        for (int i = 0; i < valSamples.size(); i++) {
            valInputs[i] = valSamples.get(i).input;
            valLabels[i][0] = valSamples.get(i).label;
        }
        //Use ND4J to format inputs
        INDArray valInput = Nd4j.create(valInputs);
        INDArray valLabel = Nd4j.create(valLabels);

        //Create model
        MultiLayerNetwork model = buildModel();
        double bestValLoss = Double.MAX_VALUE;
        int epochsWithoutImprovement = 0;

        // Index list used for shuffling rather than the raw arrays
        // to avoid the cost of copying large float[][] data on every epoch.
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);

        for (int epoch = 0; epoch < 10; epoch++) {
            // Shuffle sample order each epoch to prevent the model from overfitting
            // to positional patterns in the data (e.g. always seeing openings first).
            Collections.shuffle(indices);

            int totalBatches = (int) Math.ceil((double) n / batchSize);
            int batches = 0;
            double trainLoss = 0;

            // Mini-batch training loop — feed the model one small batch at a time
            // rather than the full dataset. This gives more frequent weight updates
            // and lower peak memory usage than training on all samples at once.
            for (int i = 0; i < n; i += batchSize) {
                // Clamp end index so the final batch handles remaining samples
                // cleanly even if n is not evenly divisible by batchSize.
                int end = Math.min(i + batchSize, n);
                int size = end - i;

                float[][] batchInputs = new float[size][781];
                float[][] batchLabels = new float[size][1];

                // Build batch using shuffled indices so each epoch sees a different ordering.
                for (int j = 0; j < size; j++) {
                    batchInputs[j] = inputs[indices.get(i + j)];
                    batchLabels[j] = labels[indices.get(i + j)];
                }

                model.fit(Nd4j.create(batchInputs), Nd4j.create(batchLabels));
                batches++;

                // Print training loss periodically — model.score() returns the loss
                // from the most recent batch, not the epoch average.
                if (batches % 500 == 0) {
                    trainLoss = model.score();
                    System.out.println("Epoch " + epoch + " - batch " + batches
                        + "/" + totalBatches + " - loss: " + trainLoss);
                }
            }

            // Evaluate on the held-out validation set after each full epoch.
            // Val loss is more reliable than train loss for measuring generalization
            // since the model never trains on these samples.
            double valLoss = model.score(new DataSet(valInput, valLabel));
            System.out.println("Epoch " + epoch + " - train: " + trainLoss + " val: " + valLoss);

            if (valLoss < bestValLoss) {
                // New best found — save the model immediately so the best weights
                // are preserved even if later epochs overfit and are stopped early.
                bestValLoss = valLoss;
                epochsWithoutImprovement = 0;
                ModelSerializer.writeModel(model, "new_model.zip", true);
                System.out.println("New best saved");
            } else if (++epochsWithoutImprovement >= 5) {
                // Validation loss has not improved for 5 consecutive epochs —
                // further training is likely to overfit. Stop and use the saved best.
                System.out.println("Early stopping at epoch " + epoch);
                break;
            }
        }
        System.out.println("Training complete, best val loss: " + bestValLoss);
    }
}