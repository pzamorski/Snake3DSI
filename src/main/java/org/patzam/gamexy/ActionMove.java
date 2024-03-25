package org.patzam.gamexy;

import java.util.List;
import java.util.Random;


public enum ActionMove {
    MOVE_UP, MOVE_RIGHT,MOVE_DOWN, MOVE_LEFT;

    private static final List<ActionMove> VALUES = List.of(values());
    private static final int SIZE = VALUES.size();
    private static final Random RANDOM = new Random();

    public static ActionMove getActionByIndex(final int index) {
        return VALUES.get(index);
    }
}
