package byow.Core;

import byow.TileEngine.TETile;

public class Avatar {
    private Position avatarPos;
    private int health;


    public Avatar(Position startingPos) {
        avatarPos = startingPos;
        health = 5;

    }

    public Position getAvatarPos() {
        return avatarPos;
    }

    public void setAvatarPos(Position newPos) {
        avatarPos = newPos;
    }

//    public void move(char inputChar) {
//        int[] currentPos = avatarPos.getPosition();
//        int[] newPos = ;
//        if (inputChar == 'W') {
//            currentPos[1] += 1;
//        } else if (inputChar == 'A') {
//            currentPos[0] -= 1;
//        } else if (inputChar == 'S') {
//            currentPos[1] -= 1;
//        } else if (inputChar == 'D') {
//            currentPos[0] += 1;
//        }
//
//        if (moveValid(newPos)) {
//            avatarPos = new Position(newPos[0], newPos[1]);
//        }
//    }
//
//    private boolean moveValid(int[] position) {
//
//    }




}
