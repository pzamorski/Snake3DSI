package org.patzam.gamexy;


import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.nd4j.linalg.api.ndarray.INDArray;


import java.awt.*;
import java.io.IOException;

public class App  {


    private App() {
            final Game2Snake game = new Game2Snake();
//            add(game);
//            setResizable(false);
//            pack();
//            setTitle("Snake SI");
//            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);




        final Thread thread = new Thread(() -> {

            // Create our training environment
            final GameEnvironmentMDP mdp = new GameEnvironmentMDP(game);


            final QLearningDiscreteDense<GameContext> dql = new QLearningDiscreteDense<>(
                    mdp,
                    NetworkManagerXY.buildDQNFactory(),
                    NetworkManagerXY.buildConfig()
            );


            dql.train();

            mdp.close();

            // Save network
            try {

                dql.getNeuralNet().save("test2Snake.zip");

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
            // Reset the game
            game.init();

            // Evaluate just trained network
            evaluateNetwork(game);
        });

        thread.start();
    }

    private void evaluateNetwork(Game2Snake game) {
        final MultiLayerNetwork multiLayerNetwork = NetworkManagerXY.loadNetwork("test2Snake.zip");
        int highscore = 0;
        for (int i = 0; i < 1000; i++) {
            int score = 0;
            while (game.isOngoing()) {
                try {
                    final GameContext state = game.buildStateObservation();
                    game.setSnakeLength(3);
                    final INDArray output = multiLayerNetwork.output(state.getMatrix(), false);
                    double[] data = output.data().asDouble();

                    int maxValueIndex = GameContextManager.getMaxValueIndex(data);
                    game.changeDirection(ActionMove.getActionByIndex(maxValueIndex));
                    game.move();
                    score = game.getScore();

                    // Needed so that we can see easier what is the game doing
                    NetworkManagerXY.waitMs(1);
                } catch (final Exception e) {
                    System.out.println(e.getMessage());
                    Thread.currentThread().interrupt();
                  //  game.endGame();
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
