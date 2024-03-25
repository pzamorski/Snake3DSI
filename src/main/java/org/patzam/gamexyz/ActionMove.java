package org.patzam.gamexyz;

import java.util.List;


public enum ActionMove {
    MOVE_UP, MOVE_RIGHT,MOVE_DOWN, MOVE_LEFT, MOVE_IN, MOVE_OUT;

    private static final List<ActionMove> VALUES = List.of(values());
    public static ActionMove getActionByIndex(final int index) {
        return VALUES.get(index);
    }
}
