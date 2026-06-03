package ml;

/**
 * Creates the layers and nodes for the neural network and in training 
 * effectively measures loss and adjusts weights and biases. 
 * 
 * @author Matthew-Vanhoomissen
 */

import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;

import java.util.*;
import java.io.*;

public class ModelTrainer {

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

    public static void train(List<TrainingDataGen.Sample> samples) throws IOException {
        // Center labels
        float mean = 0;
        for (TrainingDataGen.Sample s : samples) mean += s.label;
        mean /= samples.size();
        for (TrainingDataGen.Sample s : samples) 
            s.label = Math.max(-1.0f, Math.min(1.0f, s.label - mean));

        int splitIndex = (int)(samples.size() * 0.8);
        List<TrainingDataGen.Sample> trainSamples = samples.subList(0, splitIndex);
        List<TrainingDataGen.Sample> valSamples   = samples.subList(splitIndex, samples.size());

        int n = trainSamples.size();
        int batchSize = 32;

        float[][] inputs = new float[n][781];
        float[][] labels = new float[n][1];
        for (int i = 0; i < n; i++) {
            inputs[i] = trainSamples.get(i).input;
            labels[i][0] = trainSamples.get(i).label;
        }

        // Val arrays — built once
        float[][] valInputs = new float[valSamples.size()][781];
        float[][] valLabels = new float[valSamples.size()][1];
        for (int i = 0; i < valSamples.size(); i++) {
            valInputs[i] = valSamples.get(i).input;
            valLabels[i][0] = valSamples.get(i).label;
        }
        INDArray valInput = Nd4j.create(valInputs);
        INDArray valLabel = Nd4j.create(valLabels);

        MultiLayerNetwork model = buildModel();
        double bestValLoss = Double.MAX_VALUE;
        int epochsWithoutImprovement = 0;

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);

        for (int epoch = 0; epoch < 30; epoch++) {
            Collections.shuffle(indices);
            int totalBatches = (int) Math.ceil((double) n / batchSize);
            int batches = 0;
            double trainLoss = 0;

            for (int i = 0; i < n; i += batchSize) {
                int end = Math.min(i + batchSize, n);
                int size = end - i;
                float[][] batchInputs = new float[size][781];
                float[][] batchLabels = new float[size][1];
                for (int j = 0; j < size; j++) {
                    batchInputs[j] = inputs[indices.get(i + j)];
                    batchLabels[j] = labels[indices.get(i + j)];
                }
                model.fit(Nd4j.create(batchInputs), Nd4j.create(batchLabels));
                batches++;
                if (batches % 500 == 0) {
                    trainLoss = model.score();
                    System.out.println("Epoch " + epoch + " - batch " + batches + "/" + totalBatches + " - loss: " + trainLoss);
                }
            }

            double valLoss = model.score(new DataSet(valInput, valLabel));
            System.out.println("Epoch " + epoch + " - train: " + trainLoss + " val: " + valLoss);

            if (valLoss < bestValLoss) {
                bestValLoss = valLoss;
                epochsWithoutImprovement = 0;
                ModelSerializer.writeModel(model, "best_model.zip", true);
                System.out.println("New best saved");
            } else if (++epochsWithoutImprovement >= 5) {
                System.out.println("Early stopping at epoch " + epoch);
                break;
            }
        }
        System.out.println("Training complete, best val loss: " + bestValLoss);
    }
}