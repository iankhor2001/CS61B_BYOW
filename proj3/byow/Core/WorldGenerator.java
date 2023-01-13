package byow.Core;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;
import byow.Core.RandomUtils.*;
import org.eclipse.jetty.websocket.client.masks.RandomMasker;

public class WorldGenerator {
    private final static TETile BACKGROUND = Tileset.NOTHING;
    private final int WIDTH;
    private final int HEIGHT;
    private final Random RANDOM;
    private final int MIN_NUMBER_OF_MAIN_ROOM;
    private final int MAX_NUMBER_OF_MAIN_ROOM;
    private RoomGenerator rg;
    private TETile[][] world;

    public WorldGenerator(int width, int height, long seed, int minRoom, int maxRoom) {
        WIDTH = width;
        HEIGHT = height;
        System.out.println("World Seed: " + seed);
        RANDOM = new Random(seed);
        MIN_NUMBER_OF_MAIN_ROOM = minRoom;
        MAX_NUMBER_OF_MAIN_ROOM = maxRoom;
        world = new TETile[WIDTH][HEIGHT];
        rg = new RoomGenerator(world, HEIGHT, WIDTH, RANDOM);
    }

    public void generateWorld() {
        fillWorld(world);

        int targetNumOfMainRoom = RandomUtils.uniform(RANDOM, MIN_NUMBER_OF_MAIN_ROOM,
                MAX_NUMBER_OF_MAIN_ROOM);
        int numOfMainRoom = 0;
        int numberOfTriesFailed = 0;
        while (numOfMainRoom < targetNumOfMainRoom) {
            int roomHeight = RandomUtils.uniform(RANDOM, 5, 10);
            int roomWidth = RandomUtils.uniform(RANDOM, 5, 10);
            int roomPosX = RandomUtils.uniform(RANDOM, WIDTH);
            int roomPosY = RandomUtils.uniform(RANDOM, HEIGHT);
            Position roomPos = new Position(roomPosX, roomPosY);
            int flag = rg.drawRectangle(roomHeight, roomWidth, roomPos);
            if (flag == 1) {
                numOfMainRoom += 1;
                numberOfTriesFailed = 0;
            } else {
                numberOfTriesFailed += 1;
            }

            if (numberOfTriesFailed > 5) {
                break;
            }
        }
        checkWallsAndRemove(world);
        System.out.println("World All Checked, World Generation Complete.");
    }

    public TETile[][] getWorld() {
        return world;
    }

    public int[] getAvatarStartingPosition() {
        int[] startingPos = rg.randomPositionInRoom(0);
        return startingPos;
    }

    public static void fillWorld(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = BACKGROUND;
            }
        }
    }

    // To remove static and width/height
    private void checkWallsAndRemove(TETile[][] tiles) {
        System.out.println("Enter Checking Phase.");

        // Check if floor is connected to NOTHING
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                boolean isNothing = tiles[x][y] == BACKGROUND;
                if (isNothing) {
                    try {
                        if (tiles[x - 1][y] == Tileset.FLOOR | tiles[x + 1][y] == Tileset.FLOOR |
                                tiles[x][y - 1] == Tileset.FLOOR | tiles[x][y + 1] == Tileset.FLOOR) {
                            System.out.println("Adding Wall to (" + x + ", " + y + ")");
                            tiles[x][y] = Tileset.WALL;
                        }
                    } catch (Exception exception) {
                        if ((x - 1) != -1) {
                            if (tiles[x - 1][y] == Tileset.FLOOR) {
                                tiles[x][y] = Tileset.WALL;
                                System.out.println("Adding Wall to (" + x + ", " + y + ")");
                                continue;
                            }
                        }
                        if ((x + 1) != WIDTH) {
                            if (tiles[x + 1][y] == Tileset.FLOOR) {
                                tiles[x][y] = Tileset.WALL;
                                System.out.println("Adding Wall to (" + x + ", " + y + ")");
                                continue;
                            }
                        }
                        if ((y - 1) != -1) {
                            if (tiles[x][y - 1] == Tileset.FLOOR) {
                                tiles[x][y] = Tileset.WALL;
                                System.out.println("Adding Wall to (" + x + ", " + y + ")");
                                continue;
                            }
                        }
                        if ((y + 1) != HEIGHT) {
                            if (tiles[x][y + 1] == Tileset.FLOOR) {
                                tiles[x][y] = Tileset.WALL;
                                System.out.println("Adding Wall to (" + x + ", " + y + ")");
                                continue;
                            }
                        }
                    }
                }
            }
        }

        // Check if walls is only 1 layer thick and connecting two floors, replace if not
        /*
        for (int x = 1; x < WIDTH - 1; x += 1) {
            for (int y = 1; y < HEIGHT - 1; y += 1) {
                boolean isWall = tiles[x][y] == Tileset.WALL;
                if (isWall & x < WIDTH - 2 & y < HEIGHT - 2) {
                    boolean isTwoWallHorizontal = tiles[x + 1][y] == Tileset.WALL & tiles[x + 2][y] == Tileset.FLOOR;
                    boolean isTwoWallVertical = tiles[x][y + 1] == Tileset.WALL & tiles[x][y + 2] == Tileset.FLOOR;
                    if (WorldGenerator.containsTileInSurrounding(tiles, new int[]{x, y}, BACKGROUND)) {
                        continue;
                    }
                    if (isTwoWallHorizontal) {
                        tiles[x + 1][y] = Tileset.FLOOR;
                    }
                    if (isTwoWallVertical) {
                        tiles[x][y + 1] = Tileset.FLOOR;
                    }
                }

            }
        }
        */

        // Check if Wall is blocking a corridor
        /*
        for (int x = 1; x < WIDTH - 1; x += 1) {
            for (int y = 1; y < HEIGHT - 1; y += 1) {
                boolean isWall = tiles[x][y] == Tileset.WALL;
                if (isWall) {
                    int haveWallAtConner = tiles[x - 1][y - 1] == Tileset.WALL ? 1 : 0;
                    haveWallAtConner += tiles[x - 1][y + 1] == Tileset.WALL ? 1 : 0;
                    haveWallAtConner += tiles[x + 1][y - 1] == Tileset.WALL ? 1 : 0;
                    haveWallAtConner += tiles[x + 1][y + 1] == Tileset.WALL ? 1 : 0;
                    if (haveWallAtConner == 3 & tiles[x - 1][y] == Tileset.FLOOR & tiles[x + 1][y] == Tileset.FLOOR) {
                        tiles[x][y] = Tileset.FLOOR;
                        System.out.println("Removing wall at (" + x + ", " + y + ")");
                    } else if (haveWallAtConner == 3 & tiles[x][y - 1] == Tileset.FLOOR & tiles[x][y + 1] == Tileset.FLOOR) {
                        tiles[x][y] = Tileset.FLOOR;
                        System.out.println("Removing wall at (" + x + ", " + y + ")");
                    }
                }

            }
        }
        */

        // Check if walls is not at the outer layer of rooms or corridor
        /*
        for (int x = 1; x < WIDTH - 1; x += 1) {
            for (int y = 1; y < HEIGHT - 1; y += 1) {
                boolean isWall = tiles[x][y] == Tileset.WALL;
                if (isWall) {
                    if (tiles[x - 1][y] == Tileset.FLOOR & tiles[x + 1][y] == Tileset.FLOOR) {
                        tiles[x][y] = Tileset.FLOOR;
                    } else if (tiles[x][y - 1] == Tileset.FLOOR & tiles[x][y + 1] == Tileset.FLOOR) {
                        tiles[x][y] = Tileset.FLOOR;
                    }
                }

            }
        }
        */

    }

    public int initializeOrbs() {
        int numOfOrbs = 0;
        for (int x = 1; x < WIDTH - 1; x += 1) {
            for (int y = 1; y < HEIGHT - 1; y += 1) {
                if (world[x][y] == Tileset.FLOOR & RandomUtils.bernoulli(RANDOM, 0.03)) {
                    world[x][y] = Tileset.FLOWER;
                    numOfOrbs += 1;
                }

            }
        }
        return numOfOrbs;
    }

    private static boolean containsTileInSurrounding(TETile[][] tiles, int[] pos, TETile tileType) {
        for (int x = -1; x < 2; x += 1) {
            for (int y = -1; y < 2; y += 1) {
                if (tiles[pos[0] + x][pos[1] + y] == tileType) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        int WIDTH = 80;
        int HEIGHT = 30;
        long SEED = 1;
        Random RANDOM = new Random(SEED);
        int MIN_NUMBER_OF_MAIN_ROOM = 10;
        int MAX_NUMBER_OF_MAIN_ROOM = 31;

        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        fillWorld(world);

///////////////////////////// Main /////////////////////////////
        RoomGenerator rg = new RoomGenerator(world, HEIGHT, WIDTH, RANDOM);
        int targetNumOfMainRoom = RandomUtils.uniform(RANDOM, MIN_NUMBER_OF_MAIN_ROOM,
                MAX_NUMBER_OF_MAIN_ROOM);
        int numOfMainRoom = 0;
        int numberOfTriesFailed = 0;
        while (numOfMainRoom < targetNumOfMainRoom) {
//            int operationNumber = RandomUtils.uniform(RANDOM, 1);
//            if (operationNumber == 0) {
            int roomHeight = RandomUtils.uniform(RANDOM, 5, 10);
            int roomWidth = RandomUtils.uniform(RANDOM, 5, 10);
            int roomPosX = RandomUtils.uniform(RANDOM, WIDTH);
            int roomPosY = RandomUtils.uniform(RANDOM, HEIGHT);
            Position roomPos = new Position(roomPosX, roomPosY);
            int flag = rg.drawRectangle(roomHeight, roomWidth, roomPos);
            if (flag == 1) {
                numOfMainRoom += 1;
                numberOfTriesFailed = 0;
            } else {
                numberOfTriesFailed += 1;
            }

            if (numberOfTriesFailed > 5) {
                break;
            }
//        checkWallsAndRemove(world);


///////////////////////////// TEST /////////////////////////////
//        RoomGenerator rg = new RoomGenerator(world, HEIGHT, WIDTH, RANDOM);
//        int numOfMainRoom = 0;
//        int N = 10;
//        while (numOfMainRoom < N){
//            int roomHeight = RandomUtils.uniform(RANDOM, 5, 10);
//            int roomWidth = RandomUtils.uniform(RANDOM, 5, 10);
//            int roomPosX = RandomUtils.uniform(RANDOM, WIDTH);
//            int roomPosY = RandomUtils.uniform(RANDOM, HEIGHT);
//            Position roomPos = new Position(roomPosX, roomPosY);
//            numOfMainRoom += rg.drawRectangle(roomHeight, roomWidth, roomPos);
//        }
//        checkWallsAndRemove(world);
//    }

        }
        ter.renderFrame(world);
    }
}


