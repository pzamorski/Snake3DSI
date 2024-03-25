package org.patzam.gamexy;



import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;



public class GameEnvironmentMDP implements MDP<GameContext, Integer, DiscreteSpace> {
    private final DiscreteSpace actionSpace = new DiscreteSpace(4);
    private final Game2Snake game;

    public GameEnvironmentMDP(final Game2Snake game) {
        this.game = game;
        this.game.start();
    }

    @Override
    public ObservationSpace<GameContext> getObservationSpace() {
        return new GameObservationSpace();
    }

    @Override
    public DiscreteSpace getActionSpace() {
        return actionSpace;
    }

    @Override
    public GameContext reset() {
        return game.init();
    }

    @Override
    public void close() {}

    public void setSnakeLenth(int snakeLenth){
        game.setSnakeLength(snakeLenth);
    }

    @Override
    public StepReply<GameContext> step(final Integer actionIndex) {
        // Find action based on action index
        final ActionMove actionToTake = ActionMove.getActionByIndex(actionIndex);

        // Change direction based on action and move the snake in that direction
        game.changeDirection(actionToTake);
        game.move();


        // Get reward
        double reward = game.calculateRewardForActionToTake(actionToTake);

        // Get current state
        final GameContext observation = game.buildStateObservation();

        return new StepReply<>(
                observation,
                reward,
                isDone(),
                "SnakeDl4j"
        );
    }

    @Override
    public boolean isDone() {
        return !game.isOngoing();
    }

    @Override
    public MDP<GameContext, Integer, DiscreteSpace> newInstance() {

        game.init();
        return new GameEnvironmentMDP(game);
    }
}
