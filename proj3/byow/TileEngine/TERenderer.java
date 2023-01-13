package byow.TileEngine;

import byow.Core.Engine;
import byow.Core.Position;
import byow.Core.WorldEngine;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.Color;
import java.awt.Font;

/**
 * Utility class for rendering tiles. You do not need to modify this file. You're welcome
 * to, but be careful. We strongly recommend getting everything else working before
 * messing with this renderer, unless you're trying to do something fancy like
 * allowing scrolling of the screen or tracking the avatar or something similar.
 */
public class TERenderer {
    private static final int TILE_SIZE = 16;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;
    private int mX;
    private int mY;
    private WorldEngine we;

    /**
     * Same functionality as the other initialization method. The only difference is that the xOff
     * and yOff parameters will change where the renderFrame method starts drawing. For example,
     * if you select w = 60, h = 30, xOff = 3, yOff = 4 and then call renderFrame with a
     * TETile[50][25] array, the renderer will leave 3 tiles blank on the left, 7 tiles blank
     * on the right, 4 tiles blank on the bottom, and 1 tile blank on the top.
     * @param w width of the window in tiles
     * @param h height of the window in tiles.
     */
    public void initialize(int w, int h, int xOff, int yOff, String s) {
        this.width = w;
        this.height = h;
        this.xOffset = xOff;
        this.yOffset = yOff;
        this.we = we;
        StdDraw.setCanvasSize(width * TILE_SIZE, height * TILE_SIZE);
        Font font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);      
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);
        StdDraw.clear(new Color(0, 0, 0));
        StdDraw.enableDoubleBuffering();
        if (s != "m") {
            smenu(s);
        } else {
            dmenu();
        }
        StdDraw.show();

        Font fontSmall = new Font("Monaco", Font.BOLD, 14);
        StdDraw.setFont(fontSmall);
    }

    /**
     * Initializes StdDraw parameters and launches the StdDraw window. w and h are the
     * width and height of the world in number of tiles. If the TETile[][] array that you
     * pass to renderFrame is smaller than this, then extra blank space will be left
     * on the right and top edges of the frame. For example, if you select w = 60 and
     * h = 30, this method will create a 60 tile wide by 30 tile tall window. If
     * you then subsequently call renderFrame with a TETile[50][25] array, it will
     * leave 10 tiles blank on the right side and 5 tiles blank on the top side. If
     * you want to leave extra space on the left or bottom instead, use the other
     * initializatiom method.
     * @param w width of the window in tiles
     * @param h height of the window in tiles.
     */
    public void initialize(int w, int h) {
        initialize(w, h, 0, 0, "m");
    }

    public void setWorldEngine(WorldEngine we) {
        this.we = we;
    }

    /**
     * Takes in a 2d array of TETile objects and renders the 2d array to the screen, starting from
     * xOffset and yOffset.
     *
     * If the array is an NxM array, then the element displayed at positions would be as follows,
     * given in units of tiles.
     *
     *              positions   xOffset |xOffset+1|xOffset+2| .... |xOffset+world.length
     *                     
     * startY+world[0].length   [0][M-1] | [1][M-1] | [2][M-1] | .... | [N-1][M-1]
     *                    ...    ......  |  ......  |  ......  | .... | ......
     *               startY+2    [0][2]  |  [1][2]  |  [2][2]  | .... | [N-1][2]
     *               startY+1    [0][1]  |  [1][1]  |  [2][1]  | .... | [N-1][1]
     *                 startY    [0][0]  |  [1][0]  |  [2][0]  | .... | [N-1][0]
     *
     * By varying xOffset, yOffset, and the size of the screen when initialized, you can leave
     * empty space in different places to leave room for other information, such as a GUI.
     * This method assumes that the xScale and yScale have been set such that the max x
     * value is the width of the screen in tiles, and the max y value is the height of
     * the screen in tiles.
     * @param world the 2D TETile[][] array to render
     */
    public void renderFrame(TETile[][] world) {
        int numXTiles = world.length;
        int numYTiles = world[0].length;
        StdDraw.clear(new Color(0, 0, 0));
        for (int x = 0; x < numXTiles; x += 1) {
            for (int y = 0; y < numYTiles; y += 1) {
                if (world[x][y] == null) {
                    throw new IllegalArgumentException("Tile at position x=" + x + ", y=" + y
                            + " is null.");
                }
                world[x][y].draw(x + xOffset, y + yOffset);
            }
        }
        dHUD(world);
        StdDraw.show();
    }

    public void pause(int ms) {
        StdDraw.pause(ms);
    }

    public Position hoveringMousePos() {
//        int xPos = Math.min((int) StdDraw.mouseX(), this.width - 1);
//        int yPos = Math.min((int) StdDraw.mouseY(), this.height - 12);

        int xPos = (int) StdDraw.mouseX();
        int yPos = (int) StdDraw.mouseY();

        Position x = new Position(xPos, yPos);
        return x;
    }

    public String hoveringType(TETile[][] world) {
        Position x = hoveringMousePos();
        int xPos = x.getX();
        int yPos = x.getY();
        if (xPos > (this.width - 1)) {
            return "Outside world";
        }
        if (yPos > (this.height - 11)) {
            return "Outside world";
        }
        if (mX != xPos || mY != yPos) {
            mX = xPos;
            mY = yPos;
        }
        String s;
        if (world[mX][mY] == Tileset.FLOOR) {
            s = "Floor";
        } else if (world[mX][mY] == Tileset.WALL) {
            s = "Wall";
        } else if (world[mX][mY] == Tileset.FLOWER) {
            s = "Light Orb";
        } else if (world[mX][mY] == Tileset.NOTHING) {
            s = "Outside world";
        } else {
            s = "Avatar";
        }
        return s;
    }

    public static String orbCount(WorldEngine we) {
        int orbCounts = we.getOrbsLightCounter();
        if (orbCounts > 0) {
            return "Orbs remaining: " + String.valueOf(orbCounts);
        }
        return "You win! Press :Q to quit.";
    }

    public void dHUD(TETile[][] world) {
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, 14);
        StdDraw.setFont(fontSmall);
        StdDraw.line(0, height - 3, width, height - 3);
        StdDraw.text(6, height - 2, hoveringType(world));
        StdDraw.text(width - 6, height - 2, Engine.time());
        StdDraw.text(width / 2, height - 2, orbCount(we));
        StdDraw.show();
    }

    public void dmenu() {
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 40);
        StdDraw.setFont(fontBig);
        StdDraw.text(width / 2, (height * 2) / 3, "BYOW GAME");

        Font fontBig1 = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontBig1);
        StdDraw.text(width / 2, height / 2, "NEW GAME (N)");
        StdDraw.text(width / 2, (height / 2) - 2, "LOAD GAME (L)");
        StdDraw.text(width / 2, (height / 2) - 4, "QUIT (Q)");


    }

    public void smenu(String s) {
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontBig);
        StdDraw.text(width / 2, (height * 2) / 3, "Seed : " + s);
//        pause(10);
    }
}


