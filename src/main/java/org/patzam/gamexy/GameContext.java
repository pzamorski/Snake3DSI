package org.patzam.gamexy;

import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class GameContext implements Encodable {
    private final double[] inputs;

    public GameContext(final double[] inputs) {
        this.inputs = inputs;
        for (double input : inputs) {
            System.out.print(input+" ");
        }
        System.out.println();
    }

    @Override
    public double[] toArray() {
        return inputs;
    }

    @Override
    public boolean isSkipped() {
        return false;
    }

    @Override
    public INDArray getData() {
        return Nd4j.create(inputs);
    }

    public INDArray getMatrix() {
        return Nd4j.create(new double[][] {
                inputs
        });
    }

    @Override
    public Encodable dup() {
        return null;
    }
}
