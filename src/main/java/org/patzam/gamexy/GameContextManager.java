package org.patzam.gamexy;



import java.util.Arrays;

public final class GameContextManager {

    private GameContextManager() {}


    public static double getStateForDirection(final Position[] snakePosition,
                                              final Position foodPosition,
                                              final Direction directionToCheck) {
        // Position of snakes head
        final Position head = snakePosition[0];
        final Position nextPosition = PositionManager.getNextPosition(head, directionToCheck);

        if (isHeadUnableToMoveToNextPosition(nextPosition, snakePosition)) {
            return -1.0;
        }

        if (directionToCheck == Direction.UP) {
            if (head.getY() < foodPosition.getY()) {
                return 1.0;
            }

            return 0.0;
        }

        if (directionToCheck == Direction.RIGHT) {
            if (head.getX() < foodPosition.getX()) {
                return 1.0;
            }

            return 0.0;
        }

        if (directionToCheck == Direction.DOWN) {
            if (head.getY() < foodPosition.getY()) {
                return 1.0;
            }

            return 0.0;
        }

        if (directionToCheck == Direction.LEFT) {
            if (head.getX() < foodPosition.getX()) {
                return 1.0;
            }

            return 0.0;
        }

        return 0.0;
    }

    public static int getMaxValueIndex(final double[] values) {
        int maxAt = 0;

        for (int i = 0; i < values.length; i++) {
            maxAt = values[i] > values[maxAt] ? i : maxAt;
        }

        return maxAt;
    }

    private static boolean isHeadUnableToMoveToNextPosition(final Position nextPosition,
                                                            final Position[] snakePosition) {
        return nextPosition.isOutsideTheArea() || Arrays.asList(snakePosition).contains(nextPosition);
    }
}
