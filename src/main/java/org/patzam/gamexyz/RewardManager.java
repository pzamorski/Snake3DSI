package org.patzam.gamexyz;


import java.util.Arrays;


public final class RewardManager {

    private RewardManager() {}

    public static double calculateRewardForActionToTake(final ActionMove action,
                                                        final Position[] snakePosition,
                                                        final Position foodPosition) {
        Direction nextDirection = Direction.UP;
        switch (action) {
            case MOVE_UP:
                break;
            case MOVE_RIGHT:
                nextDirection = Direction.RIGHT;
                break;
            case MOVE_DOWN:
                nextDirection = Direction.DOWN;
                break;
            case MOVE_LEFT:
                nextDirection = Direction.LEFT;
                break;
            case MOVE_IN:
                nextDirection = Direction.IN;
                break;
            case MOVE_OUT:
                nextDirection = Direction.OUT;
                break;
            default:
                break;
        }

        final Position position = PositionManager.getNextPosition(
                snakePosition[0],
                nextDirection
        );

        return getRewardForPosition(nextDirection, position, snakePosition, foodPosition);
    }

    private static double getRewardForPosition(final Direction nextDirection,
                                               final Position nextPosition,
                                               final Position[] snakePosition,
                                               final Position foodPosition) {



        if (nextPosition.isOutsideTheArea()) {
            return -100.0;
        }

        if (Arrays.asList(snakePosition).contains(nextPosition)) {
            return -100.0;
        }

        if (nextPosition.equals(foodPosition)) {
            return 1000.0;
        }

        if (PositionManager.isProximityToFoodPosition(nextPosition, foodPosition, nextDirection,snakePosition[0])) {
            return 2.0;
        }
        return -1.0;
    }
}
