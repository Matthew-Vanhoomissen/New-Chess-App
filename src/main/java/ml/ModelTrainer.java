package ml;

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
            .updater(new Adam(0.001))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(781).nOut(512)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(512).nOut(256)
                .activation(Activation.RELU)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(256).nOut(128)
                .activation(Activation.RELU)
                .build())
            .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(128).nOut(1)
                .activation(Activation.TANH)
                .build())
            .build();

        MultiLayerNetwork model = new MultiLayerNetwork(config);
        model.init();
        return model;
    }

    public static void train(List<TrainingDataGen.Sample> samples) throws IOException {
        MultiLayerNetwork model = buildModel();

        int splitIndex = (int)(samples.size() * 0.8);
        List<TrainingDataGen.Sample> trainSamples = samples.subList(0, splitIndex);
        List<TrainingDataGen.Sample> valSamples = samples.subList(splitIndex, samples.size());

        int n = trainSamples.size();
        int batchSize = 32;

        float[][] inputs = new float[n][781];
        float[][] labels = new float[n][1];

        for (int i = 0; i < n; i++) {
            inputs[i] = trainSamples.get(i).input;
            labels[i][0] = trainSamples.get(i).label;
        }

        model.setListeners(new ScoreIterationListener(100));
        for (int epoch = 0; epoch < 10; epoch++) {
            int totalBatches = (int) Math.ceil((double) n / batchSize);
            int batches = 0;
            double trainLoss = 0;
            for (int i = 0; i < n; i += batchSize) {
                int end = Math.min(i + batchSize, n);

                // Slice out just this batch
                float[][] batchInputs = Arrays.copyOfRange(inputs, i, end);
                float[][] batchLabels = Arrays.copyOfRange(labels, i, end);

                INDArray inputArray = Nd4j.create(batchInputs);
                INDArray labelArray = Nd4j.create(batchLabels);

                model.fit(inputArray, labelArray);
                batches++;
                if (batches % 100 == 0) {
                    trainLoss = model.score();
                    System.out.println("Epoch " + epoch + " - batch " + batches + "/" + totalBatches + " - loss: " + trainLoss);
                }
            }
            // After each epoch
            float[][] valInputs = new float[valSamples.size()][781];
            float[][] valLabels = new float[valSamples.size()][1];
            for (int i = 0; i < valSamples.size(); i++) {
                valInputs[i] = valSamples.get(i).input;
                valLabels[i][0] = valSamples.get(i).label;
            }

            INDArray valInput = Nd4j.create(valInputs);
            INDArray valLabel = Nd4j.create(valLabels);
            double valLoss = model.score(new DataSet(valInput, valLabel));
            System.out.println("Epoch " + epoch + " - train loss: " + trainLoss + " val loss: " + valLoss);
        }

        ModelSerializer.writeModel(model, "chess_model3.zip", true);
        System.out.println("Model saved");
    }
}