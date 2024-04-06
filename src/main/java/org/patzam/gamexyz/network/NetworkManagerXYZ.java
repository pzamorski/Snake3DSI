package org.patzam.gamexyz.network;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.nd4j.linalg.learning.config.RmsProp;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class NetworkManagerXYZ {
    public static final int NUMBER_OF_INPUTS =6;
    public static final double LOW_VALUE = -1;
    public static final double HIGH_VALUE = 1;

    public static QLearningConfiguration buildConfig() {
        return QLearningConfiguration.builder()
                .seed(123L)
                .maxEpochStep(1200) //1200
                .maxStep(4000)//4000
                .expRepMaxSize(400000)//40000
                .batchSize(128)//128
                .targetDqnUpdateFreq(500)//500
                .updateStart(20)//20
                .rewardFactor(0.92)//0.12
                .gamma(0.995)//0.995
                .errorClamp(0.5)//0.5f
                .minEpsilon(0.1f)//0.1f
                .epsilonNbStep(1000)//1000
                .doubleDQN(true)
                .build();
    }

    public static DQNFactoryStdDense buildDQNFactory() {
        final DQNDenseNetworkConfiguration build = DQNDenseNetworkConfiguration.builder()
                .l2(0.003)//0.003
                .updater(new RmsProp(0.01))//0.000025

                .numHiddenNodes(5000)//1000
                .numLayers(1)//1
                .build();


        return new DQNFactoryStdDense(build);
    }
    public static MultiLayerNetwork loadNetwork(final String networkName) {
        try {
            return MultiLayerNetwork.load(new File(networkName), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void waitMicroseconds(final long ms) {
        if (ms == 0) {
            return;
        }

        try {
            TimeUnit.MICROSECONDS.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
