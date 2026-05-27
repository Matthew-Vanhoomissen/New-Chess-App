package ml;

import game.Board;

import java.io.IOException;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

public class ChessEvaluator {
    private MultiLayerNetwork model;

    public ChessEvaluator(MultiLayerNetwork model) {
        this.model = model;
    }

    public float evaluate(Board board) {
        float[] input = BoardEncoder.convertBoard(board);
        INDArray ndInput = Nd4j.create(input).reshape(1, 781);
        INDArray output = model.output(ndInput);
        return output.getFloat(0); // single value between -1 and 1
    }

    public void save(String path) throws IOException {
        ModelSerializer.writeModel(model, path, true);
    }

    public static ChessEvaluator load(String path) throws IOException {
        MultiLayerNetwork model = ModelSerializer.restoreMultiLayerNetwork(path);
        return new ChessEvaluator(model);
    }
}