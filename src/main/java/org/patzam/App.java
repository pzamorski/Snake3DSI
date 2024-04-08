package org.patzam;


import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.patzam.game.Game;
import org.patzam.manager.GameContextManager;
import org.patzam.move.ActionMove;
import org.patzam.network.GameContext;
import org.patzam.network.GameEnvironmentMDP;
import org.patzam.network.NetworkManager;

import java.awt.*;

public class App {


    private static final String NAME_FILE = "models/model.zip";

    private App() {
        final Game game = new Game();
        game.setStatusText("Train");


        final Thread trainThread = new Thread(() -> {
            final GameEnvironmentMDP mdp = new GameEnvironmentMDP(game);
            final QLearningDiscreteDense<GameContext> dql = new QLearningDiscreteDense<>(
                    mdp,
                    NetworkManager.buildDQNFactory(),
                    NetworkManager.buildConfig()
            );

            //dql.train();
            mdp.close();
            //NetworkManager.save(dql, NAME_FILE);

            game.init();
            evaluateNetwork(game);
        });

        trainThread.start();
    }

    private void evaluateNetwork(Game game) {
        game.setStatusText("In game");
        final MultiLayerNetwork multiLayerNetwork = NetworkManager.loadNetwork(NAME_FILE);

        int highscore = 0;
        for (int i = 0; i < 10000; i++) {
            int score = 0;
            while (game.isOngoing()) {
                try {
                    final GameContext state = game.buildStateObservation();
                    final INDArray output = multiLayerNetwork.output(state.getMatrix(), false);
                    double[] data = output.data().asDouble();

                    int maxValueIndex = GameContextManager.getMaxValueIndex(data);
                    game.changeDirection(ActionMove.getActionByIndex(maxValueIndex));
                    game.move();
                    score = game.getScore();

                    // Needed so that we can see easier what is the game doing
                    NetworkManager.waitMicroseconds(1);
                } catch (final Exception e) {
                    System.out.println(e.getMessage());
                    Thread.currentThread().interrupt();
                    game.endGame();
                }
            }

            System.out.println("Iteration:" + i + "  score:" + score);
            if (score > highscore) {
                highscore = score;
            }

            // Reset the game
            game.init();
        }
        System.out.println("End evaluation");
        System.out.println("Highscore: " + highscore);
    }


    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            App ex = new App();
        });
    }
}
