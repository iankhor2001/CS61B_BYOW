package byow.Core;

import byow.InputDemo.KeyboardInputSource;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    public static final String[] SAVE_FILES = {"save0.txt", "save1.txt", "save2.txt"};
    private static final boolean PRINT_TYPED_KEYS = true;

    private long seed;
    private WorldEngine we;
    private StringIterator inputIter;
    private String inputString;
    private boolean colonFlag;
    private TETile[][] activeWorldFrame;

    private class StringIterator implements Iterator<Character>{
        private final String input;
        private int index;

        public StringIterator(String s) {
            index = 0;
            input = s.toUpperCase();
        }

        public Character next() {
            Character returnChar = input.charAt(index);
            index += 1;
            return returnChar;
        }

        public boolean hasNext() {
            return index < input.length();
        }
    }

    private String loadFile(int saveNumber) {
        String saveFile = SAVE_FILES[saveNumber];
        In in = new In(saveFile);
        String s = in.readAll();
        in.close();
        return s;
    }

    public void saveProgress(int loadFileNumber) {
        System.out.println("\nAll Input: \n" + inputString);
        Out out = new Out(SAVE_FILES[loadFileNumber]);
        out.println(inputString);
        out.close();
        System.out.println("File saved.\n");
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        inputString = "";
        TETile[][] finalWorldFrame = new TETile[HEIGHT][WIDTH];
        ter.initialize(WIDTH, HEIGHT + 10, 0, 0, "m");

        System.out.println("Input Command: ");
        int loadFileNumber = 1;
        char c = getNextKey();
        ter.initialize(WIDTH, HEIGHT + 10, 0, 0, " ");
        switch (c) {
            case 'Q':
                System.out.println("Quiting.");
                break;
            case 'N':
            case 'L':
                // Get Load File
                while (true) {
                    try {
//                        char loadFileCh = getNextKey();
//                        loadFileNumber = Integer.parseInt(String.valueOf(loadFileCh));
                        loadFileNumber = 1;
                        if (loadFileNumber < 4 & loadFileNumber != 0) {
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("Exception: " + e);
                        System.out.println("Try again.");
                    }

                }
                WorldEngine we = generateMainMenu(c, loadFileNumber);
            default:
                break;
        }
        ter.setWorldEngine(we);
        if (we != null) {
            we.updateActiveWorld();
            activeWorldFrame = we.getActiveWorld();
            ter.renderFrame(activeWorldFrame);
            colonFlag = false;

            while (true) {
                Character ch = Character.toUpperCase(getNextKey());
                if (ch == 'Q' & colonFlag == true) {
                    ter.pause(1000);
                    System.out.println("Quiting.");
                    break;
                }
                doAction(ch);
                ter.pause(200);
                ter.renderFrame(activeWorldFrame);
            }

            this.saveProgress(loadFileNumber);
        }
        System.exit(0);
    }

    private Character getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                Character ch = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (PRINT_TYPED_KEYS) {
                    System.out.println("Input: " + ch);
                }
                return ch;
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        System.out.println(input);
        inputIter = new StringIterator(input);
        TETile[][] finalWorldFrame = new TETile[HEIGHT][WIDTH];
        inputString = "";
        ter.initialize(WIDTH, HEIGHT);
        Character ch = inputIter.next();
        WorldEngine we = generateMainMenu(ch);

        we.updateActiveWorld();
        ter.setWorldEngine(we);
        activeWorldFrame = we.getActiveWorld();
        ter.renderFrame(activeWorldFrame);
        colonFlag = false;

        while(inputIter.hasNext()) {
            ch = inputIter.next();
            if (ch == 'Q' & colonFlag == true) {
                ter.pause(1000);
                System.out.println("Quiting.");
                break;
            }
            doAction(ch);
            ter.pause(200);
            ter.renderFrame(activeWorldFrame);
        }
        saveProgress(0);
        finalWorldFrame = TETile.copyOf(activeWorldFrame);
        return finalWorldFrame;
    }

    public static String time() {
        Calendar c = Calendar.getInstance();
        TimeZone t = c.getTimeZone();
        Date d = c.getTime();

        SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String xdate = date.format(d);
        return xdate;
    }

    private WorldEngine generateMainMenu(Character ch) {
        return generateMainMenu(ch, 0);
    }

    private WorldEngine generateMainMenu(Character ch, int loadFileNumber) {
        switch (ch) {
            case 'N':
                String seedStr = "";
                getNewSeed();
                we = new WorldEngine(WIDTH, HEIGHT, seed);
                break;
            case 'L':
                // TODO: load function
                String str = loadFile(loadFileNumber);
                StringIterator strIter = new StringIterator(str);
                String seedStrN = "";
                while (strIter.hasNext()) {
                    ch = strIter.next();
                    if (ch == '\n') {
                        System.out.println("Seed: " + seedStrN);
                        inputString += seedStrN + '\n';
                        seed = Integer.parseInt(seedStrN);
                        break;
                    }
                    seedStrN += ch;
                }
                we = new WorldEngine(WIDTH, HEIGHT, seed);
                ter.setWorldEngine(we);
                we.updateActiveWorld();
                while (strIter.hasNext()) {
                    ch = strIter.next();
                    doAction(ch);
                }
                break;
            default:
                System.out.println("Invalid Input.");
                break;
        }
        return we;
    }

    private void getNewSeed() {
        Character ch;
        String seedStr = "";
        while (true) {
            seedStr = "";
            try {
                while (true) {
                    ter.initialize(WIDTH, HEIGHT + 10, 0, 0, seedStr);
                    if (inputIter != null) {
                        ch = inputIter.next();
                    } else {
                        ch = getNextKey();
                    }
                    System.out.println("in: " + ch);
                    if ((ch == 'S') | (ch == 's')) {
                        inputString += seedStr + '\n';
                        seed = Integer.parseInt(seedStr);
                        break;
                    }
                    seedStr += ch;
                }
                break;
            } catch (Exception e) {
                System.out.println("Exception: " + e);
                System.out.println("Try again.");
            }
        }
    }
    private void doAction(Character ch) {
        switch(ch) {
            case ':':
                colonFlag = true;
                // TODO: Display colon
                break;
            case 'Q':
                if (colonFlag) {
                    return;
                }
                System.out.println("Need ':' colon to quit.");
                break;
            case 'O':
                we.toggleOrbLight();
                we.updateActiveWorld();
                activeWorldFrame = we.getActiveWorld();
                ter.renderFrame(activeWorldFrame);
                inputString += ch;
                break;
            case 'W':
            case 'A':
            case 'S':
            case 'D':
                we.moveAvatar(ch);
                activeWorldFrame = we.getActiveWorld();
                ter.renderFrame(activeWorldFrame);
                inputString += ch;
                break;
            case 'N':
                we.toggleLight();
                we.updateActiveWorld();
                activeWorldFrame = we.getActiveWorld();
                ter.renderFrame(activeWorldFrame);
                inputString += ch;
                break;
            case '\n':
            case '\r':
                break;
            default:
                System.out.println("Invalid Input: " + ch);
                break;
        }
        return;
    }



    public static void main(String[] args) {
        Engine engine = new Engine();
//        engine.interactWithInputString("N1s");
//        engine.interactWithInputString("N34ssa:Q");
//        engine.interactWithInputString("Lw");

        engine.interactWithKeyboard();
        //engine.interactWithInputString("N134Swwa");
    }
}
