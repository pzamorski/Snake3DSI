package org.patzam.gamexyz;


import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.patzam.gamexyz.game.Game;
import org.patzam.gamexyz.manager.GameContextManager;
import org.patzam.gamexyz.network.GameContext;
import org.patzam.gamexyz.network.GameEnvironmentMDP;
import org.patzam.gamexyz.network.NetworkManagerXYZ;


import java.awt.*;

public class App  {


    private App() {
            final Game game = new Game();
            game.status="Train";



        final MultiLayerNetwork multiLayerNetwork = NetworkManagerXYZ.loadNetwork("backup.zip");

        final Thread thread = new Thread(() -> {
            final GameEnvironmentMDP mdp = new GameEnvironmentMDP(game);
            final QLearningDiscreteDense<GameContext> dql = new QLearningDiscreteDense<>(
                    mdp,
                    NetworkManagerXYZ.buildDQNFactory(),
                    NetworkManagerXYZ.buildConfig()
            );











//            dql.train();
            mdp.close();

//            try {
//                dql.getNeuralNet().save("test2Snake.zip");
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }

            game.init();
            evaluateNetwork(game);
        });

        thread.start();
    }

    private void evaluateNetwork(Game game) {
        game.status="In game";
        final MultiLayerNetwork multiLayerNetwork = NetworkManagerXYZ.loadNetwork("best.zip");
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
                    NetworkManagerXYZ.waitMicroseconds(0);
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
