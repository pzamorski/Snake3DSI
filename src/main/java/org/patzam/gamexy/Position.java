package org.patzam.gamexy;


public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isOutsideTheArea() {
        return x >= GameParameters.GAME_DIMENSIONS || y >= GameParameters.GAME_DIMENSIONS || x < 0 || y < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    public double calcDistance(Position p2) {

        double x2 = p2.getX();
        double y2 = p2.getY();




        return Math.sqrt(Math.pow(x2 - this.x, 2) + Math.pow(y2 - this.y, 2));
    }

    public int getAbsoluteDifferenceX(Position p2) {
        return Math.abs(p2.getX() - this.x);
    }

    public int getAbsoluteDifferenceY(Position p2) {
        return Math.abs(p2.getY() - this.y);
    }

}
