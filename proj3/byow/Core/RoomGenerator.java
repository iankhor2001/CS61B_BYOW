package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class RoomGenerator {
    private static final int X_THEN_Y = 0;
    private static final int Y_THEN_X = 1;
    private static final boolean PRINT_OUTPUT = false;
    private static final boolean CONNECT_CORRIDOR = false;
//    private final int UNION_LENGTH;
    private TETile[][] world;
    private final int worldHeight;
    private final int worldWidth;
    private boolean[][] isOccupied;
    private static Random RANDOM;
    private List<Room> rooms;

//    private WeightedQuickUnionUF floorSet;
//    private boolean[][] isFloor;
    private List<Room> roomsAndCorridors;

    private class Room {
        private final Position topRightVertex;
        private final Position topLeftVertex;
        private final Position bottomLeftVertex;
        private final Position bottomRightVertex;
        private final int id;
        public Room(Position topRight, Position topLeft, Position botRight, Position botLeft, int id){
            this.topRightVertex = topRight;
            this.topLeftVertex = topLeft;
            this.bottomLeftVertex = botLeft;
            this.bottomRightVertex = botRight;
            this.id = id;
        }
        public int getId() {
            return id;
        }
        public int getLeftSideXAxis() {
            return topLeftVertex.getX();
        }
        public int getRightSideXAxis() {
            return topRightVertex.getX();
        }
        public int getTopSideYAxis() {
            return topLeftVertex.getY();
        }
        public int getBottomSideYAxis() {
            return bottomLeftVertex.getY();
        }
        public double[] getCenter() {
            double[] center = new double[2];
            center[0] = (double) (topLeftVertex.getX() - bottomRightVertex.getX()) / 2;
            center[1] = (double) (topLeftVertex.getY() - bottomRightVertex.getY()) / 2;
            return center;
        }

        public double distanceTo(Room r) {
            double[] curr = getCenter();
            double[] ot = r.getCenter();
            double y1 = ot[1];
            double y2 = curr[1];
            double x1 = ot[0];
            double x2 = curr[0];
            return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
        }
    }

    // Constructor.
    public RoomGenerator(TETile[][] world, int height, int width, Random RANDOM) {
        this.world = world;
        worldHeight = height;
        worldWidth = width;
        isOccupied = new boolean[width][height];
        rooms = new ArrayList<Room>();
        roomsAndCorridors = new ArrayList<Room>();
        this.RANDOM = RANDOM;

//        isFloor = new boolean[height][width];
//        floorSet = new WeightedQuickUnionUF(height * width + 1);
//        UNION_LENGTH = height * width;
    }

    /* ----------------------------------- Helpers ----------------------------------- */

    public int size() {
        return rooms.size();
    }

    private void placeTile(int posX, int posY, TETile tileType) {
        if (tileType == Tileset.FLOOR) {
            world[posX][posY] = Tileset.FLOOR;
//            newFloor(posY, posX);
        } else if (tileType == Tileset.WALL) {
            if (world[posX][posY] == Tileset.FLOOR) {
                return;
            }
            world[posX][posY] = Tileset.WALL;
        }
    }

    private void placeDiagonalTiles(int[] pos, TETile tileType) {
        placeTile(pos[0] - 1, pos[1] - 1, tileType);
        placeTile(pos[0] + 1, pos[1] + 1, tileType);
        placeTile(pos[0] + 1, pos[1] - 1, tileType);
        placeTile(pos[0] - 1, pos[1] + 1, tileType);
    }

    private static int[] randomPosition(Room room) {
        int x1 = room.getLeftSideXAxis();
        int x2 = room.getRightSideXAxis();
        int y1 = room.getBottomSideYAxis();
        int y2 = room.getTopSideYAxis();
        int x = RandomUtils.uniform(RANDOM,x1 + 1, x2);
        int y = RandomUtils.uniform(RANDOM, y1 + 1, y2);
        int[] xY = {x, y};
        return xY;
    }

    public int[] randomPositionInRoom(int roomNumber) {
        Room room = rooms.get(roomNumber);
        int[] randomPos = randomPosition(room);
        return randomPos;
    }


    /* ----------------------------------- Placing Rooms ----------------------------------- */

    /** Generate Room using the given Position object as reference point.
     * The reference point will be the bottom-left point of the rectangle.
     * @param height, width, pos */
    public int drawRectangle(int height, int width, Position bottomLeft) {
        int[] position = bottomLeft.getPosition();
        int x_pos = position[0];
        int y_pos = position[1];
        int x_max = x_pos + width;
        int y_max = y_pos + height;

        if (x_max > (worldWidth - 1)) {
            x_max = worldWidth - 1;
        }
        if (y_max > (worldHeight - 1)) {
            y_max = worldHeight - 1;

        }

        if ((x_max - x_pos) < 4 | (y_max - y_pos) < 4) {
            return 0;
        }

        // Check if overlapping
        Position topRight = new Position(x_max, y_max);
        if (isOverlapping(bottomLeft, topRight)) {
            return 0;
        }

        if (PRINT_OUTPUT) {
            System.out.println("Placing room at " + x_pos + " " + y_pos);
        }

        for (int i = x_pos; i < x_max; i += 1) {
            for (int j = y_pos; j < y_max; j += 1) {
                if (i == x_pos | i == (x_max - 1) | j == y_pos | j == (y_max - 1)) {
                    placeTile(i, j, Tileset.WALL);
                } else {
                    placeTile(i, j, Tileset.FLOOR);
                }
                isOccupied[i][j] = true;
            }
        }
        Room newRoom = new Room(new Position(x_max, y_max),
                new Position(x_pos, y_max),
                new Position(x_max, y_pos),
                bottomLeft,
                rooms.size());
        rooms.add(newRoom);
        roomsAndCorridors.add(newRoom);

        // Connect the room to the main connected portion
        if (rooms.size() > 1) {
            roomConnector(newRoom);
        }
        return 1;
    }

    /** Connect room to the closest room. */
    public void roomConnector(Room thisRoom) {
        if (rooms.size() <= 1) {
            return;
        }

        Room closestRoom;
        int closestRoomId = findClosestRoom(thisRoom);
        if (CONNECT_CORRIDOR) {
            closestRoom = roomsAndCorridors.get(closestRoomId);
        } else {
            closestRoom = rooms.get(closestRoomId);
        }


        int[] thisRoomPos = randomPosition(thisRoom);
        int[] closestRoomPos = randomPosition(closestRoom);


        double angle = findAngle(thisRoom, closestRoom);
        int direction;
        if (angle <= 45) {
            direction = X_THEN_Y;
        } else if (angle <= 90) {
            direction = Y_THEN_X;
        } else if (angle <= 135) {
            direction = Y_THEN_X;
        } else if (angle <= 180) {
            direction = X_THEN_Y;
        } else if (angle <= 225) {
            direction = X_THEN_Y;
        } else if (angle <= 270) {
            direction = Y_THEN_X;
        } else if (angle <= 315) {
            direction = Y_THEN_X;
        } else {
            direction = X_THEN_Y;
        }
        if (PRINT_OUTPUT) {
            System.out.println("Connecting rooms at " + Arrays.toString(thisRoomPos) + " " + Arrays.toString(closestRoomPos));
        }
        connectCorridors(thisRoomPos, closestRoomPos, direction);

        // 1. Choose Side (1,2,3,4 Clockwise from north)
        // 2. Connect with straight line is the sides have overlapping part
        // 3. Connect to the designated side if no side is overlapping

        // First draw the corridor with the walls into a blank TETile[][],
        // and also randomized size (if side is big enough ).
        // Then overlap the main world with the TETile[][],
        // floor will always replace walls.


    }

    private void connectCorridors(int[] pos1, int[] pos2, int direction) {
        int x_pos1 = pos1[0], y_pos1 = pos1[1], x_pos2 = pos2[0], y_pos2 = pos2[1];
        int startX, startY, endX, endY;
        if (x_pos1 < x_pos2) {
            startX = x_pos1;
            endX = x_pos2;
        } else {
            startX = x_pos2;
            endX = x_pos1;
        }
        if (y_pos1 < y_pos2) {
            startY = y_pos1;
            endY = y_pos2;
        } else {
            startY = y_pos2;
            endY = y_pos1;
        }
        if (direction == 0) {
            for (int x = startX; x <= endX; x++) {
                placeTile(x, pos1[1] + 1, Tileset.WALL);
                placeTile(x, pos1[1], Tileset.FLOOR);
                placeTile(x, pos1[1] - 1, Tileset.WALL);

            }
            for (int y = startY; y <= endY; y++) {
                placeTile(pos2[0] - 1, y, Tileset.WALL);
                placeTile(pos2[0], y, Tileset.FLOOR);
                placeTile(pos2[0] + 1, y, Tileset.WALL);

            }
            if (CONNECT_CORRIDOR) {
                Room newCorridorX = new Room(new Position(endX + 1, pos1[1] + 1),
                        new Position(startX, pos1[1] + 1),
                        new Position(endX + 1, pos1[1] - 1),
                        new Position(startX, pos1[1] - 1),
                        roomsAndCorridors.size());
                roomsAndCorridors.add(newCorridorX);
                Room newCorridorY = new Room(new Position(pos2[0] + 1, endY),
                        new Position(pos2[0] - 1, endY),
                        new Position(pos2[0] + 1, startY - 1),
                        new Position(pos2[0] - 1, startY - 1),
                        roomsAndCorridors.size());
                roomsAndCorridors.add(newCorridorY);
            }
            placeDiagonalTiles(new int[]{pos2[0], pos1[1]}, Tileset.WALL);

        } else {
            for (int y = startY; y <= endY; y++) {
                placeTile(pos1[0] - 1, y, Tileset.WALL);
                placeTile(pos1[0], y, Tileset.FLOOR);
                placeTile(pos1[0] + 1, y, Tileset.WALL);
            }
            for (int x = startX; x <= endX; x++) {
                placeTile(x, pos2[1] + 1, Tileset.WALL);
                placeTile(x, pos2[1], Tileset.FLOOR);
                placeTile(x, pos2[1] - 1, Tileset.WALL);
            }
            if (CONNECT_CORRIDOR) {
                Room newCorridorY = new Room(new Position(pos1[0] + 1, endY + 1),
                        new Position(pos1[0] - 1, endY + 1),
                        new Position(pos1[0] + 1, startY),
                        new Position(pos1[0] - 1, startY),
                        roomsAndCorridors.size());
                roomsAndCorridors.add(newCorridorY);
                Room newCorridorX = new Room(new Position(endX, pos2[1] + 1),
                        new Position(startX - 1, pos2[1] + 1),
                        new Position(endX, pos2[1] - 1),
                        new Position(startX - 1, pos2[1] - 1),
                        roomsAndCorridors.size());
                roomsAndCorridors.add(newCorridorX);
            }
            placeDiagonalTiles(new int[]{pos1[0], pos2[1]}, Tileset.WALL);
        }
        placeDiagonalTiles(pos1, Tileset.WALL);
        placeDiagonalTiles(pos2, Tileset.WALL);
    }

    private int findClosestRoom(Room thisRoom) {
        int closestId = -1;
        double closestDist = (double) (worldHeight + worldWidth);
//        for (Room room : rooms) {
        for (Room room : roomsAndCorridors) {
            if (room == thisRoom) {
                continue;
            }
            double distance = thisRoom.distanceTo(room);
            if (distance < closestDist) {
                closestDist = distance;
                closestId = room.getId();
            }
        }
        return closestId;
    }

    // Cite: https://stackoverflow.com/questions/9970281/java-calculating-the-angle-between-two-points-in-degrees
    private double findAngle(Room thisRoom, Room targetRoom) {
        double[] thisRoomCenter = thisRoom.getCenter();
        double[] targetRoomCenter = targetRoom.getCenter();

        double angle = Math.toDegrees(
                Math.atan2(targetRoomCenter[1] - thisRoomCenter[1],
                        targetRoomCenter[0] - thisRoomCenter[0]));
        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    /* ----------------------------------- Checking Rooms ----------------------------------- */

    private boolean isOverlapping(Position bottomLeft, Position topRight) {
        int x1 = bottomLeft.getX();
        int y1 = bottomLeft.getY();
        int x2 = topRight.getX() + 1;
        int y2 = topRight.getY() + 1;

        if (x1 != 0) {
            x1 -= 1;
        }
        if (y1 != 0) {
            y1 -= 1;
        }
        if (x2 >= worldWidth) {
            x2 = worldWidth;
        }
        if (y2 >= worldHeight) {
            y2 = worldHeight;
        }
        for (int i = x1; i < x2; i += 1) {
            for (int j = y1; j < y2; j += 1) {
                if (isOccupied[i][j] == true) {
                    return true;
                }
            }
        }
        return false;
    }

    // Set (To be removed)
//    private int xYTo1D(int row, int col) {
//        return row * worldWidth + col;
//    }
//
//    private void newFloor(int row, int col) {
//        if (row > worldHeight || col > worldWidth) {
//            throw new java.lang.IndexOutOfBoundsException("Parameter is not valid.");
//        }
//        if (isFloor[row][col]) {
//            return;
//        }
//        isFloor[row][col] = true;
//        if ((row + 1) < worldHeight && isFloor[row + 1][col]) {
//            floorSet.union(xYTo1D(row, col), xYTo1D(row + 1, col));
//        }
//        if ((col + 1) < worldWidth && isFloor[row][col + 1]) {
//            floorSet.union(xYTo1D(row, col), xYTo1D(row, col + 1));
//        }
//        if ((row - 1) > -1 && isFloor[row - 1][col]) {
//            floorSet.union(xYTo1D(row, col), xYTo1D(row - 1, col));
//        }
//        if ((col - 1) > -1 && isFloor[row][col - 1]) {
//            floorSet.union(xYTo1D(row, col), xYTo1D(row, col - 1));
//        }
//    }
//
//    /* !Only call this when all floors are created. */
//    public void unionAllTilesExceptFloor() {
//        for (int x = 0; x < worldWidth; x += 1) {
//            for (int y = 0; y < worldHeight; y += 1) {
//                if (world[x][y] != Tileset.FLOOR) {
//                    floorSet.union(xYTo1D(y, x), UNION_LENGTH);
//                }
//            }
//        }
//    }
//
//    public int countSeparatedRooms() {
//        return floorSet.count();
//    }


}
