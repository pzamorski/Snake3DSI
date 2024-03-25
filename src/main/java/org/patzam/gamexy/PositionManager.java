package org.patzam.gamexy;

public final class PositionManager {

    public static Position getNextPosition(final Position currentPosition,
                                           final Direction direction) {
        if (direction == Direction.UP) {
            return createNewPosition(
                    currentPosition.getX(),
                    currentPosition.getY() - GameParameters.PLAYER_SIZE
            );
        }

        if (direction == Direction.RIGHT) {
            return createNewPosition(
                    currentPosition.getX() + GameParameters.PLAYER_SIZE,
                    currentPosition.getY()
            );
        }

        if (direction == Direction.DOWN) {
            return createNewPosition(
                    currentPosition.getX(),
                    currentPosition.getY() + GameParameters.PLAYER_SIZE
            );
        }

        return createNewPosition(
                currentPosition.getX() - GameParameters.PLAYER_SIZE,
                currentPosition.getY()
        );
    }

    public static boolean isProximityToFoodPosition(final Position nextPosition,
                                                    final Position foodPosition,
                                                    final Direction nextDirection, Position snakePosition) {

        int absoluteDifferenceXsnakePosition = snakePosition.getAbsoluteDifferenceX(foodPosition);
        int absoluteDifferenceYsnakePosition = snakePosition.getAbsoluteDifferenceY(foodPosition);

        int absoluteDifferenceXnextPosition = nextPosition.getAbsoluteDifferenceX(foodPosition);
        int absoluteDifferenceYnextPosition = nextPosition.getAbsoluteDifferenceY(foodPosition);

//        System.out.println("                                                                 "+
//                absoluteDifferenceXsnakePosition+"   "+absoluteDifferenceYsnakePosition+"/"+
//                absoluteDifferenceXnextPosition+"   "+absoluteDifferenceYnextPosition);

        if (nextDirection == Direction.UP) {
            return absoluteDifferenceYnextPosition < absoluteDifferenceYsnakePosition;
        }

        if (nextDirection == Direction.RIGHT) {
            return absoluteDifferenceXnextPosition < absoluteDifferenceXsnakePosition;
        }

        if (nextDirection == Direction.DOWN) {
            return absoluteDifferenceYnextPosition < absoluteDifferenceYsnakePosition;
        }

        if (nextDirection == Direction.LEFT) {
            return absoluteDifferenceXnextPosition < absoluteDifferenceXsnakePosition;
        }

        return false;
    }

    private static Position createNewPosition(final int x, final int y) {
        return new Position(x, y);
    }
}
