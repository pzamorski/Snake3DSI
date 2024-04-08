package move;


import com.jme3.math.Vector3f;
import game.GameParameters;

public class Position {
    private final int x;
    private final int y;
    private final int z;


    public Position(int x, int y,int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Position(Position position) {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
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

    public  Vector3f ToVector3f(){
        return new Vector3f(getX(), getY(), getZ());
    }

    public boolean isOutsideTheArea() {
        return x >= GameParameters.BOX_DIMENSIONS_X || y >= GameParameters.BOX_DIMENSIONS_Y || z >= GameParameters.BOX_DIMENSIONS_Z || x < 0 || y < 0 || z < 0;
    }

    public boolean areAnyCoordinatesChanged(Position position){
        if (position==null){
            return true;
        }
        return x != position.getX() || y != position.getY() || z != position.getZ();
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

    public double calcDistance(Position position) {

        double x2 = position.getX();
        double y2 = position.getY();
        double z2 = position.getZ();

        return Math.sqrt(Math.pow(x2 - this.x, 2) + Math.pow(y2 - this.y, 2) + Math.pow(z2 - this.z, 2));
    }

    public int getAbsoluteDifferenceX(Position position) {
        return Math.abs(position.getX() - this.x);
    }

    public int getAbsoluteDifferenceY(Position position) {
        return Math.abs(position.getY() - this.y);
    }

    public int getAbsoluteDifferenceZ(Position position) {
        return Math.abs(position.getZ() - this.z);
    }

}
