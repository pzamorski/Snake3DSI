import game.Game;
import manager.GameContextManager;
import move.ActionMove;
import network.GameContext;
import network.GameEnvironmentMDP;
import network.NetworkManager;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.File;
import java.util.Scanner;


public class App {
    private static final String MODELS_DIR = "models/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Choose between training or evaluating the network
        System.out.println("Choose the operation: ");
        System.out.println("1. Train the network");
        System.out.println("2. Evaluate the network");
        System.out.print("Enter your choice (type 1 or 2): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            trainModel(scanner);
        } else if (choice == 2) {
            evaluateModel(scanner);
        } else {
            System.out.println("Invalid choice. Please choose 1 or 2.");
        }

        scanner.close();
    }

    private static void trainModel(Scanner scanner) {
        System.out.println("Enter the name for the model file (leave blank to use the default name): ");
        String modelName = scanner.nextLine();
        if (modelName.isEmpty()) {
            modelName = "bestModel.zip"; // Default model name
        }

        // Initialize the game and training environment
        Game game = new Game();
        game.setStatusText("Train");
        GameEnvironmentMDP mdp = new GameEnvironmentMDP(game);
        QLearningDiscreteDense<GameContext> dql = new QLearningDiscreteDense<>(mdp, NetworkManager.buildDQNFactory(), NetworkManager.buildConfig());

        // Train the model
        dql.train();
        mdp.close();
        NetworkManager.save(dql, MODELS_DIR + modelName+".zip");
    }

    private static void evaluateModel(Scanner scanner) {
        // List available model files
        File modelsDirectory = new File(MODELS_DIR);
        File[] modelFiles = modelsDirectory.listFiles();
        if (modelFiles != null && modelFiles.length > 0) {
            System.out.println("Available models for evaluation:");
            for (File file : modelFiles) {
                System.out.println("   -" + file.getName().replace(".zip",""));
            }

            // Ask for the model file name
            System.out.println("Enter the name of the model name for evaluation: ");
            String modelName = scanner.nextLine();

            // Check if the provided model name exists
            File modelFile = new File(MODELS_DIR + modelName+".zip");
            if (modelFile.exists()) {
                evaluateNetwork(modelName+".zip");
            } else {
                System.out.println("Invalid model file name. Please enter a valid model file name.");
            }
        } else {
            System.out.println("No models available for evaluation in the models directory.");
        }
    }

    private static void evaluateNetwork(String modelName) {
        // Initialize the game and load the model
        Game game = new Game();
        game.init();
        game.setStatusText("In game");
        MultiLayerNetwork multiLayerNetwork = NetworkManager.loadNetwork(MODELS_DIR + modelName);
        int score = 0;

        // Evaluate the network
        while (game.isOngoing()) {
            try {
                GameContext state = game.buildStateObservation();
                INDArray output = multiLayerNetwork.output(state.getMatrix(), false);
                double[] data = output.data().asDouble();

                int maxValueIndex = GameContextManager.getMaxValueIndex(data);
                game.changeDirection(ActionMove.getActionByIndex(maxValueIndex));
                game.move();
                score = game.getScore();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                Thread.currentThread().interrupt();
                game.endGame();
            }
        }
        game.stop();
        System.out.println("Score: " + score);
    }
}
