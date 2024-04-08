package org.patzam;

import junit.framework.TestCase;
import org.patzam.game.Game;
import org.patzam.game.GameParameters;
import org.patzam.move.ActionMove;
import org.patzam.move.Direction;
import org.patzam.move.Position;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GameTest extends TestCase {

    public void testSnakeLengthAfterInit() throws NoSuchFieldException, IllegalAccessException {
        Game game = new Game();
        game.init();

        Field snakeLengthField = Game.class.getDeclaredField("snakeLength");
        snakeLengthField.setAccessible(true);
        int snakeLength = (int) snakeLengthField.get(game);

        assertEquals(GameParameters.DEFAULT_SNAKE_LENGTH, snakeLength);
    }

    public void testMove() throws NoSuchFieldException, IllegalAccessException {
        Game game = new Game();
        game.init();

        game.move();

        Field snakePositionField = Game.class.getDeclaredField("snakePosition");
        snakePositionField.setAccessible(true);
        Position[] snakePosition = (Position[]) snakePositionField.get(game);

        assertTrue(snakePosition[0] != null);
    }

    public void testChangeDirection() throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        Game game = new Game();
        game.init();

        Method changeDirectionMethod = Game.class.getDeclaredMethod("changeDirection", ActionMove.class);
        changeDirectionMethod.setAccessible(true);
        try {
            changeDirectionMethod.invoke(game, ActionMove.MOVE_LEFT);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        Field currentDirectionField = Game.class.getDeclaredField("currentDirection");
        currentDirectionField.setAccessible(true);
        Direction currentDirection = (Direction) currentDirectionField.get(game);

        assertEquals(Direction.RIGHT, currentDirection);
    }
    
}