package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class WorldEngine {
    private final int WIDTH;
    private final int HEIGHT;
    private static final int MIN_NUMBER_OF_MAIN_ROOM = 10;
    private static final int MAX_NUMBER_OF_MAIN_ROOM = 12;
    private WorldGenerator wg;
    private TETile[][] world;
    private TETile[][] activeWorld;
    private boolean light;
    private boolean orbLight;
    private int orbsLightCounter;
    private Avatar avatar;

    private int numOfOrbs;

    public WorldEngine(int width, int height, long seed) {
        WIDTH = width;
        HEIGHT = height;
        wg = new WorldGenerator(WIDTH, HEIGHT, seed, MIN_NUMBER_OF_MAIN_ROOM, MAX_NUMBER_OF_MAIN_ROOM);
        initializeWorld();
    }

    private void initializeWorld() {
        wg.generateWorld();
        light = true;
        orbLight = true;
        int[] startingCoor = wg.getAvatarStartingPosition();

        numOfOrbs = wg.initializeOrbs();
        orbsLightCounter = 0;
        world = wg.getWorld();

        avatar = new Avatar(new Position(startingCoor[0], startingCoor[1]));
        activeWorld = TETile.copyOf(world);
        activeWorld = createActiveWorld();

    }



    public TETile[][] getWorld() {
        return world;
    }

    public TETile[][] createActiveWorld() {
        Position avatarPos = avatar.getAvatarPos();
        if (light & (orbsLightCounter <= 0)) {
            WorldGenerator.fillWorld(activeWorld);
            lightWorld(activeWorld, avatarPos);
            for (int x = 0; x < WIDTH; x += 1) {
                for (int y = 0; y < HEIGHT; y += 1) {
                    if (orbLight & (world[x][y] == Tileset.FLOWER)) {
                        activeWorld[x][y] = Tileset.FLOWER;
                    }
                }
            }
        } else {
            activeWorld = TETile.copyOf(world);
        }
        activeWorld[avatarPos.getX()][avatarPos.getY()] = Tileset.AVATAR;
        return activeWorld;

    }

    public void toggleLight() {
        light = !light;
    }

    public void toggleOrbLight() { orbLight = !orbLight; }

    public int getOrbsLightCounter() { return numOfOrbs; }

    private TETile[][] lightWorld(TETile[][] activeWorld, Position avatarPos) {
        int x1 = avatarPos.getX();
        int y1 = avatarPos.getY();
        int limitXFront = x1 - 3;
        int limitYBottom = y1 - 3;
        int limitXBack = x1 + 3;
        int limitYTop = y1 + 3;

        limitXFront = limitXFront < 0 ? 0 : limitXFront;
        limitXBack = limitXBack > WIDTH ? WIDTH : limitXBack;
        limitYBottom = limitYBottom < 0 ? 0 : limitYBottom;
        limitYTop = limitYTop > HEIGHT ? HEIGHT : limitYTop;

        for (int i = limitXFront; i < limitXBack; i++) {
            for (int j = limitYBottom; j < limitYTop; j++) {
                activeWorld[i][j] = world[i][j];
            }
        }
        return activeWorld;
    }

    public void updateActiveWorld() {
        activeWorld = createActiveWorld();
    }

    public TETile[][] getActiveWorld() {
        return activeWorld;
    }

    public void moveAvatar(Character inputChar) {
        int[] currentPos = avatar.getAvatarPos().getPosition();
        int[] newPos = new int[]{currentPos[0], currentPos[1]};
        if (inputChar == 'W') {
            newPos[1] += 1;
        } else if (inputChar == 'A') {
            newPos[0] -= 1;
        } else if (inputChar == 'S') {
            newPos[1] -= 1;
        } else if (inputChar == 'D') {
            newPos[0] += 1;
        }
        if (isValidMove(newPos)) {
            System.out.println("Moving: " + inputChar);
            avatar.setAvatarPos(new Position(newPos));
            if (world[newPos[0]][newPos[1]] == Tileset.FLOWER) {
                numOfOrbs -= 1;
                orbsLightCounter = 5;
                world[newPos[0]][newPos[1]] = Tileset.FLOOR;
            } else {
                orbsLightCounter -= 1;
            }
            updateActiveWorld();
        } else {
            System.out.println("Invalid Move: " + inputChar);
        }
    }

    private boolean isValidMove(int[] pos) {
        int x = pos[0];
        int y = pos[1];
        if (world[x][y] != Tileset.FLOOR & world[x][y] != Tileset.FLOWER) {
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(80, 30);
        WorldEngine we = new WorldEngine(80, 30, 1);
        TETile[][] world = we.getActiveWorld();
        ter.renderFrame(world);
        System.out.println();
        ter.pause(1000);
        we.moveAvatar('S');
        world = we.getActiveWorld();
        ter.renderFrame(world);
    }
}
