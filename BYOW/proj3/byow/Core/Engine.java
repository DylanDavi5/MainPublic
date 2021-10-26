package byow.Core;


import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.io.File;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.io.BufferedOutputStream;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 90;
    /**
     * Height allocated to world generation
     */
    public static final int HEIGHT = 30;
    /**
     * Height allocated to UI
     */
    public static final int UIHEIGHT = 15;
    /**
     * Total height of the window being displayed
     */
    public static final int WINDOW_HEIGHT = HEIGHT + UIHEIGHT;
    public static final TETile[][] WORLD = new TETile[WIDTH][HEIGHT];
    public static final Player PLAYER = new Player(0, 0, WORLD);
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File SAVE_FILE = Paths.get(CWD.getPath(), "SAVE_FILE.txt").toFile();
    public static final Font DEFAULT_FONT = new Font("Monaco", Font.BOLD, 50);
    public static final int TOT_TORCHES = 5;


    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        // height of total game window
        ter.initialize(WIDTH, WINDOW_HEIGHT);
        InputSource inputSource;
        inputSource = new KeyboardInputSource();
        mainMenu(inputSource);
        StdDraw.clear();
        String input = "";
        boolean inMaiMenu = true;
        boolean inLoreMenu = false;
        boolean testBoolean = true;
        try {
            while (inputSource.possibleNextInput()) {
                Character newInput = ' ';
                if (StdDraw.hasNextKeyTyped()) {
                    newInput = Character.toUpperCase(StdDraw.nextKeyTyped());
                    input = input + newInput;
                }
                if (!inMaiMenu) {
                    displayHUD();
                }
                if (inMaiMenu && newInput == 'Q') {
                    save(input);
                    hardQuit();
                } else if (inMaiMenu && newInput == 'Z') {
                    //inMaiMenu = false;
                    //inLoreMenu= true;
                    loreMenu();
                    mainMenu(inputSource);
                } else if (inMaiMenu && newInput == 'N') {
                    inMaiMenu = false;
                    String seed = "";
                    seedPrompt(seed);
                    newInput = Character.toUpperCase(inputSource.getNextKey());
                    input = input + newInput;
                    while (newInput != 'S') {
                        seed = seed + newInput;
                        seedPrompt(seed);
                        newInput = inputSource.getNextKey();
                        input = input + newInput;
                    }
                    Random random = new Random(getSeed(seed));
                    createWorld(random);
                    render(ter);
                } else if (inMaiMenu && newInput == 'L') {
                    inMaiMenu = false;
                    //load from save file and get up to speed
                    input = load();
                    interactWithInputString(input);
                    render(ter);
                } else if (newInput == ':') {
                    newInput = inputSource.getNextKey();
                    if (newInput == 'Q') {
                        save(input);
                        hardQuit();
                    }
                } else if (newInput == 'E') {
                    playerInteract();
                    checkConditions();
                    render(ter);
                } else if (newInput == 'T') {
                    PLAYER.cycleTorch();
                    render(ter);
                } else if (newInput == 'W' || newInput == 'A'
                        || newInput == 'S' || newInput == 'D') {
                    String newInputS = String.valueOf(newInput);
                    movePlayer(newInputS);
                    checkConditions();
                    render(ter);
                } else if (newInput == 'R') {
                    PLAYER.toggleLights();
                    render(ter);
                }
            }
        } catch (IllegalStateException e) {
            System.out.println("Thanks for playing!");
        }
    }

    /**
     * Checks win and loss conditions
     */
    public static void checkConditions() throws IllegalStateException {
        if (PLAYER.getNumTorches() == TOT_TORCHES) {
            win();
        } else {
            int lossRadius = 1;
            if (calculateStepsRadius(PLAYER.getSteps()) == lossRadius) {
                lose();
            }
        }
    }

    /**
     * Called if player wins
     */
    public static void win() throws IllegalStateException {
        StdDraw.setPenColor(StdDraw.BOOK_BLUE);
        Font winFont = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(winFont);
        StdDraw.text(WIDTH / 2, WINDOW_HEIGHT / 2, "You won...but what did it cost?");
        StdDraw.show();
        softQuit();
    }

    /**
     * Called if player loses
     */
    public static void lose() throws IllegalStateException {
        StdDraw.setPenColor(StdDraw.RED);
        Font lossFont = new Font("Monaco", Font.BOLD, 50);
        StdDraw.setFont(lossFont);
        StdDraw.text(WIDTH / 2, WINDOW_HEIGHT / 2, "You lost...loser");
        StdDraw.show();
        softQuit();
    }

    /**
     * decides which renderer to use
     */
    private static void render(TERenderer ter) {
        if (PLAYER.getLightsOn()) {
            ter.renderFrame(WORLD);
        } else {
            int radius = calculateStepsRadius(PLAYER.getSteps());
            ter.renderFrameRadius(WORLD, PLAYER.getXPos(), PLAYER.getYPos(),
                    radius, PLAYER.getTorchShape());
        }
    }

    /**
     * Calculates radius of light around player having taken num steps
     */
    private static int calculateStepsRadius(int steps) {
        // radius of torch light at start of game
        int startingRadius = 10;
        // the number of steps needed to reduce torch width by 1
        int stepsToReduce = 20;
        return startingRadius * (PLAYER.getNumTorches() + 1) - (steps / stepsToReduce);
    }

    /**
     * Displays HUD components
     */
    private static void displayHUD() {
        StdDraw.setPenColor(Color.black);
        StdDraw.filledRectangle(WIDTH / 2, WINDOW_HEIGHT, WIDTH, UIHEIGHT);
        StdDraw.setFont();
        StdDraw.setPenColor(Color.CYAN);
        int horSpace = 20;
        int vertSpace = 5;
        double mouseX = StdDraw.mouseX();
        double mouseY = StdDraw.mouseY();
        //StdDraw.text(horSpace, WINDOW_HEIGHT - vertSpace, "Mouse X: " + mouseX);
        //StdDraw.text(horSpace, WINDOW_HEIGHT - vertSpace * 2, "Mouse Y: " + mouseY);
        StdDraw.text(10, WINDOW_HEIGHT - vertSpace, "Mouse X: " + mouseX);
        StdDraw.text(10, WINDOW_HEIGHT - vertSpace * 2, "Mouse Y: " + mouseY);
        StdDraw.text(horSpace, WINDOW_HEIGHT - vertSpace, "Player X: " + PLAYER.getXPos());
        StdDraw.text(horSpace, WINDOW_HEIGHT - vertSpace * 2, "Player Y: " + PLAYER.getYPos());
        StdDraw.text(horSpace * 2, WINDOW_HEIGHT - vertSpace, "Tile: "
                + getTileDescription(mouseX, mouseY));
        StdDraw.text(horSpace * 2, WINDOW_HEIGHT - vertSpace * 2, "Torches: "
                + PLAYER.getNumTorches());
        StdDraw.text(horSpace * 3, WINDOW_HEIGHT - vertSpace, "Controls: ");
        StdDraw.text(horSpace * 3 + horSpace / 2, WINDOW_HEIGHT - vertSpace, "A - Left");
        StdDraw.text(horSpace * 3 + horSpace / 2, WINDOW_HEIGHT - vertSpace * .5, "S - Down");
        StdDraw.text(horSpace * 3 + horSpace / 2, WINDOW_HEIGHT - vertSpace * 1.5, "W - Up");
        StdDraw.text(horSpace * 3 + horSpace / 2, WINDOW_HEIGHT - vertSpace * 2, "D - Right");
        StdDraw.text(horSpace * 3 + horSpace / 2, WINDOW_HEIGHT - vertSpace * 2.5, "E - Interact");
        StdDraw.text(horSpace * 4, WINDOW_HEIGHT - vertSpace, "T - Torch");
        StdDraw.text(horSpace * 4, WINDOW_HEIGHT - vertSpace * .5, "R - Lights");
        /*
        StdDraw.text(horSpace * 2, WINDOW_HEIGHT - vertSpace, "Player X: " + PLAYER.getXPos());
        StdDraw.text(horSpace * 2, WINDOW_HEIGHT - vertSpace * 2, "Player Y: " + PLAYER.getYPos());
        StdDraw.text(horSpace * 3, WINDOW_HEIGHT - vertSpace, "Tile: "
                + getTileDescription(mouseX, mouseY));
        StdDraw.text(horSpace * 3, WINDOW_HEIGHT - vertSpace * 2, "Torches: "
                + PLAYER.getNumTorches());
        StdDraw.text(horSpace * 4, WINDOW_HEIGHT - vertSpace, "Controls: ");
        StdDraw.text(horSpace * 4 + horSpace / 2, WINDOW_HEIGHT - vertSpace, "A - Left");
        StdDraw.text(horSpace * 4 + horSpace / 2, WINDOW_HEIGHT - vertSpace * .5, "S - Down");
        StdDraw.text(horSpace * 4 + horSpace / 2, WINDOW_HEIGHT - vertSpace * 1.5, "W - Up");
        StdDraw.text(horSpace * 4 + horSpace / 2, WINDOW_HEIGHT - vertSpace * 2, "D - Right");
        StdDraw.text(horSpace * 4 + horSpace / 2, WINDOW_HEIGHT - vertSpace * 2.5, "E - Interact");
         */
        StdDraw.show();
    }

    /**
     * Returns which tile is at given pixel
     */
    private static TETile getTile(double x, double y) {
        int xPos = (int) x;
        int yPos = (int) y;
        return WORLD[xPos][yPos];
    }

    /**
     * Returns description of tile at given pixel
     */
    private static String getTileDescription(double x, double y) {
        if (y >= HEIGHT) {
            return "nothing";
        } else {
            return getTile(x, y).description();
        }

    }

    /**
     * make program unresponsive
     */
    private static void softQuit() throws IllegalStateException {
        throw new IllegalStateException();
    }

    /**
     * make program unresponsive
     */
    private static void hardQuit() {
        System.exit(0);
    }


    /**
     * Set up everything needed for StdDraw canvas
     */
    private static void configureCanvas() {
        StdDraw.setCanvasSize(WIDTH * 16, WINDOW_HEIGHT * 16);
        StdDraw.setFont(DEFAULT_FONT);
        //StdDraw.setXscale(0, WIDTH);
        //StdDraw.setYscale(0, WINDOW_HEIGHT);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
    }

    /**
     * Displays main menu and accompanying options
     */
    private static void mainMenu(InputSource inputSource) {
        StdDraw.clear(Color.BLACK);
        Font menuFont = new Font("Arial", Font.BOLD, 50);
        StdDraw.setFont(menuFont);
        StdDraw.setPenColor(Color.CYAN);
        int offSet = 5;
        int menuCenterX = WIDTH / 2;
        int menuCenterY = WINDOW_HEIGHT / 2 + offSet;
        StdDraw.text(menuCenterX, menuCenterY, "Energizer II: Bunny Strikes Back");
        Font subMenuFont = new Font("Arial", Font.BOLD, 30);
        StdDraw.setFont(subMenuFont);
        StdDraw.text(menuCenterX, menuCenterY - offSet, "New Game (N)");
        StdDraw.text(menuCenterX, menuCenterY - offSet * 2, "Load Game (L)");
        StdDraw.text(menuCenterX, menuCenterY - offSet * 3, "Quit (Q)");
        StdDraw.text(menuCenterX, menuCenterY - offSet * 4, "Lore (Z)");
        StdDraw.show();
    }

    /**
     * Displays given text in middle of screen
     */
    private static void drawFrame(String s) {
        // Take the string and display it in the center of the screen
        // If game is not over, display relevant game information at the top of the screen
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.CYAN);
        Font font = new Font("Arial", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.text(WIDTH / 2, HEIGHT / 2, s);
        StdDraw.show();
    }

    /**
     * Displays game lore
     */
    private static void loreMenu() {
        int frameTime = 1500;
        drawFrame("The year is 1453, Constantinople has fallen");
        StdDraw.pause(frameTime);
        drawFrame("You play as the Energizer bunny.");
        StdDraw.pause(frameTime);
        drawFrame("But it's 1453, there are no batteries.");
        StdDraw.pause(frameTime);
        drawFrame("Instead, you fulfill your lust for power with torches.");
        StdDraw.pause(frameTime);
        drawFrame("Collect all the torches.");
        StdDraw.pause(frameTime);
        drawFrame("Maybe then you will stand a chance at retaking Constantinople.");
        StdDraw.pause(frameTime);

    }

    /**
     * Generates UI for user inputting seed
     */
    private static void seedPrompt(String seed) {
        StdDraw.clear(Color.BLACK);
        StdDraw.text(WIDTH / 2, WINDOW_HEIGHT / 2 + 10, "please enter seed");
        StdDraw.text(WIDTH / 2, WINDOW_HEIGHT / 2, seed);
        StdDraw.show();
    }

    /**
     * saves the current status to the save file
     */
    public static void save(String s) {

        //creates save file if it doesn't exist
        if (!SAVE_FILE.exists()) {
            try {
                SAVE_FILE.createNewFile();

            } catch (IOException excp) {
                throw new IllegalArgumentException(excp.getMessage());
            }

        }
        //write the string to the save file
        try {
            if (SAVE_FILE.isDirectory()) {
                throw
                        new IllegalArgumentException("cannot overwrite directory");
            }
            BufferedOutputStream str =
                    new BufferedOutputStream(Files.newOutputStream(SAVE_FILE.toPath()));
            str.write((s).getBytes(StandardCharsets.UTF_8));
            str.close();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
    }

    /**
     * loads the status from current save file
     */
    private static String load() {
        byte[] contents;

        //if save file doesn't exist then nothing is saved
        if (!SAVE_FILE.exists()) {
            return "";
        }

        //reads the contents from save file and returns it as a string
        if (!SAVE_FILE.isFile()) {
            throw new IllegalArgumentException("must be a normal file");
        }
        try {
            contents = Files.readAllBytes(SAVE_FILE.toPath());
        } catch (IOException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
        return new String(contents, StandardCharsets.UTF_8);
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quit save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        // "N#######S"

        char[] inputs = input.toCharArray();
        String inputProcessed = "";
        boolean inMaiMenu = true;
        boolean inLoreMenu = false;

        try {
            for (int i = 0; i < inputs.length; i++) {
                //Character newInput = inputs[i];
                Character newInput = Character.toUpperCase(inputs[i]);
                inputProcessed = inputProcessed + newInput;
                //create new world with a seed
                if (inMaiMenu && newInput == 'Q') {
                    save(input);
                    softQuit();
                } else if (inMaiMenu && newInput == 'N') {
                    String seed = "";
                    i++;
                    newInput = Character.toUpperCase(inputs[i]);
                    while (newInput != 'S') {
                        seed = seed + newInput;
                        i++;
                        newInput = Character.toUpperCase(inputs[i]);

                    }
                    Random random = new Random(getSeed(seed));
                    createWorld(random);
                    inMaiMenu = false;
                } else if (input.equals("L")) {
                    inMaiMenu = false;
                    //load from save file and get up to speed
                    interactWithInputString(load());
                } else if (newInput == ':') {
                    //newInput = inputs[i];
                    if (i < inputs.length - 1 && inputs[i + 1] == 'Q') {
                        //quit and sav
                        save(input);
                        softQuit();
                    }
                } else if (newInput == 'E') {
                    playerInteract();
                    //checkConditions();
                } else if (newInput == 'W' || newInput == 'A'
                        || newInput == 'S' || newInput == 'D') {
                    String newInputS = String.valueOf(newInput);
                    movePlayer(newInputS);
                }
                //update stuff
            }
        } catch (IllegalStateException e) {
            System.out.println("Thanks for playing!");
        }
        return WORLD;
    }

    /**
     * gets the seed given the user input
     */
    private long getSeed(String input) {
        long seed;

        try {
            seed = Long.parseLong(input.substring(1, input.length() - 1));
            //System.out.print(seed);
        } catch (NumberFormatException e) {
            seed = randomSEED();
            System.out.println("invalid input random seed used");
        }
        return seed;
    }

    /**
     * Connect rooms to each other from left to right
     */
    private static void connectRooms(Room[] rooms, TETile[][] world, Random random) {
        for (int i = 0; i < rooms.length - 1; i++) {
            rooms[i].connectTo(rooms[i + 1], world, random);
        }
    }

    /**
     * Connect rooms allowing for cyclic behavior
     */
    private static void connectRoomsCyclic(Room[] rooms, TETile[][] world, Random random) {
        connectRooms(rooms, world, random);
    }

    /**
     * Connect rooms from left to right
     */
    private static void connectRoomsLR(Room[] rooms, TETile[][] world, Random random) {
        // sort these by x pos
        Comparator<Room> comparator = new RoomComparator();
        // connecting leftmost rooms to each other
        Arrays.sort(rooms, comparator);
        connectRooms(rooms, world, random);
    }

    /**
     * Generates random seed
     */
    private static int randomSEED() {
        int seed = (int) Math.round(Math.random() * 1000);
        return seed;

    }

    /**
     * Makes rand num of rooms, of rand size, at rand position
     */
    private static Room[] makeRooms(TETile[][] world, Random random) {

        int minRooms = 5;
        int roomVariance = 5;
        int numberOfRooms = random.nextInt(roomVariance) + minRooms;
        int minHeight = 4;
        int heightVariance = 6;
        int minWidth = 4;
        int widthVariance = 6;
        Room[] rooms = new Room[numberOfRooms];

        for (int i = 0; i < numberOfRooms; i += 1) {
            int height = random.nextInt(heightVariance) + minHeight;
            int width = random.nextInt(widthVariance) + minWidth;
            int xPos = random.nextInt(WIDTH - (minWidth + widthVariance));
            int yPos = random.nextInt(HEIGHT - (minHeight + heightVariance));
            Room room = new Room(xPos, yPos, width, height);

            //generate new rooms until no intersections occur
            while (intersectsAny(rooms, room)) {
                xPos = random.nextInt(WIDTH - (minWidth + widthVariance));
                yPos = random.nextInt(HEIGHT - (minHeight + heightVariance));
                room = new Room(xPos, yPos, width, height);
            }

            room.drawRoom(world);
            rooms[i] = room;
        }
        return rooms;
    }

    /**
     * check to see if provided room intersects with created rooms
     */
    private static boolean intersectsAny(Room[] rooms, Room room) {
        if (rooms.length == 0) {
            return false;
        }
        for (Room r : rooms) {
            if (r != null && room.intersect(r)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Creates new Room object and draws to world
     */
    private static Room makeRoom(TETile[][] world, int height, int width, int xPos, int yPos) {
        Room newRoom = new Room(xPos, yPos, width, height);
        newRoom.drawRoom(world);
        return newRoom;
    }

    /**
     * places the given number of torches randomly in the world on floor tiles
     */
    private static void placeTorches(int numTorches, Random random) {
        int placedTorches = 0;
        while (placedTorches < numTorches) {
            int torchX = random.nextInt(WIDTH);
            int torchY = random.nextInt(HEIGHT);
            if (validTorchSpot(torchX, torchY)) {
                WORLD[torchX][torchY] = Tileset.TORCH;
                placedTorches += 1;
            }
        }

    }

    /**
     * returns true if the given position is a valid spot to put a torch
     */
    private static boolean validTorchSpot(int xPos, int yPos) {
        if (WORLD[xPos][yPos] == Room.FLOOR) {
            return true;
        }
        return false;
    }

    /**
     * Procedurally generates and returns new world
     */
    private static TETile[][] createWorld(Random random) {
        //TETile[][] newWorld = new TETile[WIDTH][HEIGHT];
        //TETile[][] newWorld = WORLD;
        //blankWorld(newWorld);
        blankWorld(WORLD);

        //Room[] rooms = makeRooms(newWorld, random);
        Room[] rooms = makeRooms(WORLD, random);
        //Can modify how rooms are connected
        //connectRoomsCyclic(rooms, newWorld, random);
        connectRoomsCyclic(rooms, WORLD, random);
        //connectRooms(rooms, newWorld, random);

        placeTorches(TOT_TORCHES, random);

        // place player in a room
        int playerStartX = rooms[0].getXPos() + 1;
        int playerStartY = rooms[0].getYPos() + 1;
        PLAYER.setPosition(playerStartX, playerStartY);
        PLAYER.draw();

        return WORLD;
    }

    /**
     * moves the player based on the given input
     */
    private static TETile[][] movePlayer(String input) {
        PLAYER.move(input);
        PLAYER.draw();
        return WORLD;
    }

    /**
     *
     */
    private static TETile[][] playerInteract() {
        PLAYER.interact();
        PLAYER.draw();
        return WORLD;
    }

    /**
     * Fill world with empty tiles
     */
    private static void blankWorld(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        Engine engine = new Engine();
        TETile[][] world = engine.interactWithInputString(args[0]);
        ter.renderFrame(world);
    }
}
