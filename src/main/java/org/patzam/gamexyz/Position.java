package org.patzam.gamexyz;


public class Position {
    private final int x;
    private final int y;
    private final int z;

    public Position(int x, int y,int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public boolean isOutsideTheArea() {
        return x >= GameParameters.GAME_DIMENSIONS_X || y >= GameParameters.GAME_DIMENSIONS_Y || z >= GameParameters.GAME_DIMENSIONS_Z || x < 0 || y < 0 || z < 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Position position = (Position) o;
        return x == position.x && y == position.y && z == position.z;
    }

    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public double calcDistance(Position p2) {

        double x2 = p2.getX();
        double y2 = p2.getY();
        double z2 = p2.getZ();

        return Math.sqrt(Math.pow(x2 - this.x, 2) + Math.pow(y2 - this.y, 2) + Math.pow(z2 - this.z, 2));
    }

    public int getAbsoluteDifferenceX(Position p2) {
        return Math.abs(p2.getX() - this.x);
    }

    public int getAbsoluteDifferenceY(Position p2) {
        return Math.abs(p2.getY() - this.y);
    }

    public int getAbsoluteDifferenceZ(Position p2) {
        return Math.abs(p2.getZ() - this.z);
    }

}
