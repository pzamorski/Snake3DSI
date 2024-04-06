package org.patzam.gamexyz.manager;

import org.patzam.gamexyz.Direction;
import org.patzam.gamexyz.game.GameParameters;
import org.patzam.gamexyz.Position;

public final class PositionManager {

    public static Position getNextPosition(final Position currentPosition,
                                           final Direction direction) {

        if (direction == Direction.UP) {
            return createNewPosition(
                    currentPosition.getX(),
                    currentPosition.getY() - GameParameters.PLAYER_SIZE,
                    currentPosition.getZ()
            );
        }

        if (direction == Direction.RIGHT) {
            return createNewPosition(
                    currentPosition.getX() + GameParameters.PLAYER_SIZE,
                    currentPosition.getY(),
                    currentPosition.getZ()
            );
        }

        if (direction == Direction.DOWN) {
            return createNewPosition(
                    currentPosition.getX(),
                    currentPosition.getY() + GameParameters.PLAYER_SIZE,
                    currentPosition.getZ()
            );
        }

        if (direction == Direction.IN) {
            return createNewPosition(
                    currentPosition.getX(),
                    currentPosition.getY(),
                    currentPosition.getZ() + GameParameters.PLAYER_SIZE
            );
        }

        if (direction == Direction.OUT) {
            return createNewPosition(
                    currentPosition.getX(),
                    currentPosition.getY(),
                    currentPosition.getZ() - GameParameters.PLAYER_SIZE
            );
        }
        return createNewPosition(
                currentPosition.getX() - GameParameters.PLAYER_SIZE,
                currentPosition.getY(),
                currentPosition.getZ()
        );
    }

    public static boolean isProximityToFoodPosition(final Position nextPosition,
                                                    final Position foodPosition,
                                                    final Direction nextDirection, Position snakePosition) {

        int absoluteDifferenceXsnakePosition = snakePosition.getAbsoluteDifferenceX(foodPosition);
        int absoluteDifferenceYsnakePosition = snakePosition.getAbsoluteDifferenceY(foodPosition);
        int absoluteDifferenceZsnakePosition = snakePosition.getAbsoluteDifferenceZ(foodPosition);

        int absoluteDifferenceXnextPosition = nextPosition.getAbsoluteDifferenceX(foodPosition);
        int absoluteDifferenceYnextPosition = nextPosition.getAbsoluteDifferenceY(foodPosition);
        int absoluteDifferenceZnextPosition = nextPosition.getAbsoluteDifferenceZ(foodPosition);
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

        if (nextDirection == Direction.IN) {
            return absoluteDifferenceZnextPosition < absoluteDifferenceZsnakePosition;
        }

        if (nextDirection == Direction.OUT) {
            return absoluteDifferenceZnextPosition < absoluteDifferenceZsnakePosition;
        }

        return false;
    }

    private static Position createNewPosition(final int x, final int y,final int z) {
        return new Position(x, y, z);
    }


}
