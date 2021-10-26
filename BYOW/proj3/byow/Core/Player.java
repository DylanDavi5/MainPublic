package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Player {
    private static final TETile PLAYERMODEL_UP = Tileset.AVATAR_UP;
    private static final TETile PLAYERMODEL_DOWN = Tileset.AVATAR_DOWN;
    private static final TETile PLAYERMODEL_LEFT = Tileset.AVATAR_LEFT;
    private static final TETile PLAYERMODEL_RIGHT = Tileset.AVATAR_RIGHT;
    private TETile PLAYERMODEL = Tileset.AVATAR_DOWN;
    private boolean lightsOn = false;
    private int xPos;
    private int yPos;
    private static TETile[][] world;
    private int stepsTaken;
    private int numTorches;
    private int torchIndex;
    private String[] torchShapes = {"square", "hexagon"};

    public Player(int x, int y, TETile[][] w) {
        this.xPos = x;
        this.yPos = y;
        stepsTaken = 0;
        // Player needs one torch for radius to work correctly
        numTorches = 0;
        torchIndex = 0;
        this.world = w;
    }

    /**
     * Draw player at current position, reset previous position
     */
    public void draw() {
        this.world[xPos][yPos] = this.PLAYERMODEL;

    }

    /**
     * Set player position to given position
     */
    public void setPosition(int x, int y) {
        xPos = x;
        yPos = y;
    }

    /**
     * Changes torch to something it isn't currently
     */
    public void cycleTorch() {
        this.torchIndex = (this.torchIndex + 1) % this.torchShapes.length;
    }

    /**
     * checks to see if moving to given position tile is allowed
     */
    private static boolean validMove(int x, int y) {
        //right now assuming only valid to move onto floors
        TETile tile = world[x][y];

        if (tile != Room.FLOOR) {
            return false;
        }
        return true;
    }

    /**
     * will move character in specified direction if allowed,
     * clearing previous tile and drawing on new one
     */
    public void move(String direction) {
        String dir = direction.toLowerCase();

        if (dir.equals("w") && validMove(xPos, yPos + 1)) { // w is up
            world[xPos][yPos] = Room.FLOOR;
            this.stepsTaken += 1;
            this.yPos += 1;
            this.PLAYERMODEL = PLAYERMODEL_UP;
        } else if (dir.equals("a") && validMove(xPos - 1, yPos)) { // a is left
            world[xPos][yPos] = Room.FLOOR;
            this.stepsTaken += 1;
            this.xPos -= 1;
            this.PLAYERMODEL = PLAYERMODEL_LEFT;
        } else if (dir.equals("s") && validMove(xPos, yPos - 1)) { // s is down
            world[xPos][yPos] = Room.FLOOR;
            this.stepsTaken += 1;
            this.yPos -= 1;
            this.PLAYERMODEL = PLAYERMODEL_DOWN;
        } else if (dir.equals("d") && validMove(xPos + 1, yPos)) { // d is right
            world[xPos][yPos] = Room.FLOOR;
            this.stepsTaken += 1;
            this.xPos += 1;
            this.PLAYERMODEL = PLAYERMODEL_RIGHT;
        }
    }

    /**
     * Checks and executes available actions within radius of player
     */
    public void interact() {
        // check radius within which player can interact with objects
        int cRad = 1;
        //checking all tiles in vicinity of player
        for (int x = getXPos() - cRad; x <= getXPos() + cRad; x += 1) {
            for (int y = getYPos() - cRad; y <= getYPos() + cRad; y += 1) {
                // Don't want to check the player
                if (x == getXPos() && y == getYPos()) {
                    continue;
                } else if (world[x][y].equals(Tileset.TORCH)) {
                    this.numTorches += 1;
                    world[x][y] = Room.FLOOR;
                }
            }
        }
    }

    /**
     * Returns the number of steps taken by this player
     */
    public int getSteps() {
        return this.stepsTaken;
    }

    /**
     * Returns number of torches player has gathered
     */
    public int getNumTorches() {
        return this.numTorches;
    }

    /**
     * toggles if the lights are on or off
     */
    public void toggleLights() {
        this.lightsOn = !this.lightsOn;
    }

    /**
     * Return x position of player
     */
    public int getXPos() {
        return this.xPos;
    }

    /**
     * Return Y position of player
     */
    public int getYPos() {
        return this.yPos;
    }

    /**
     * Return torch shape of player
     */
    public String getTorchShape() {
        return torchShapes[torchIndex];
    }

    /**
     * returns whether the lights are on
     */
    public boolean getLightsOn() {
        return this.lightsOn;
    }
}
