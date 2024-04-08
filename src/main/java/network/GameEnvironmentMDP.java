package network;



import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import move.ActionMove;
import game.Game;


public class GameEnvironmentMDP implements MDP<GameContext, Integer, DiscreteSpace> {
    private final DiscreteSpace actionSpace = new DiscreteSpace(NetworkManager.NUMBER_OF_INPUTS);
    private final Game game;

    public GameEnvironmentMDP(final Game game) {
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

    @Override
    public StepReply<GameContext> step(final Integer actionIndex) {
        final ActionMove actionToTake = ActionMove.getActionByIndex(actionIndex);
        game.changeDirection(actionToTake);
        game.move();
        double reward = game.calculateRewardForActionToTake(actionToTake);
        final GameContext observation = game.buildStateObservation();


        return new StepReply<>(observation, reward, isDone(), "Snake");
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
