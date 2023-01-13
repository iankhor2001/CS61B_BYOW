package byow.Core;

public class Position {
    private int x_pos;
    private int y_pos;
    public Position(int x, int y) {
        x_pos = x;
        y_pos = y;
    }

    public Position(int[] xAndY) {
        x_pos = xAndY[0];
        y_pos = xAndY[1];
    }

    public int[] getPosition() {
        int[] pos = {x_pos,y_pos};
        return pos;
    }

    public int getX() {
        return x_pos;
    }

    public int getY() {
        return y_pos;
    }
}
