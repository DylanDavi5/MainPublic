package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;


/**
 * Represents a room with surrounding walls
 */
public class Room {
    private int xPos;
    private int yPos;
    private int width;
    private int height;
    public static final TETile WALL = Tileset.BRICK_WALL;
    public static final TETile FLOOR = Tileset.WOOD_FLOOR;

    public Room(int x, int y, int w, int h) {
        // (xPos, yPos) is bottom left most point of room
        this.xPos = x;
        this.yPos = y;
        this.width = w; //exterior size
        this.height = h; //exterior size
    }

    /**
     * Draw room of this's dimensions in world
     * assume width and height >= 3
     */
    public void drawRoom(TETile[][] world) {
        // draw walls
        drawHorizontalLine(WALL, world, xPos, yPos, width);
        drawHorizontalLine(WALL, world, xPos, yPos + height - 1, width);
        drawVerticalLine(WALL, world, xPos, yPos, height);
        drawVerticalLine(WALL, world, xPos + width - 1, yPos, height);
        // draw floors
        for (int i = 1; i < width - 1; i++) {
            drawVerticalLine(FLOOR, world, xPos + i, yPos + 1, height - 2);
        }
    }

    /**
     * Draws vertical line of tiles of given height at pos
     */
    public void drawVerticalLine(TETile tile, TETile[][] world,
                                 int x, int y, int length) {
        for (int i = 0; i < length; i++) {
            world[x][y + i] = tile;
        }
    }

    /**
     * Draws horizontal line of tiles of given height at pos
     */
    public void drawHorizontalLine(TETile tile, TETile[][] world,
                                   int x, int y, int length) {
        for (int i = 0; i < length; i++) {
            world[x + i][y] = tile;
        }
    }

    /**
     * Returns x coordinate
     */
    public int getXPos() {
        return this.xPos;
    }

    /**
     * Returns y coordinate
     */
    public int getYPos() {
        return this.yPos;
    }

    /**
     * Returns width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns height
     */
    public int getHeight() {
        return this.height;
    }

    /*
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Room)) {
            return false;
        }
        Room r = (Room) o;
        if (this.xPos != r.getXPos() || this.yPos != r.getYPos()
                || this.width != r.getWidth() || this.height != r.getHeight()) {
            return false;
        }
        return true;

    }
     */

    /**
     * Checks if given room intersects with this
     */
    public boolean intersect(Room other) {
        // checking x position bounds
        if (this.xPos > other.getXPos() + other.getWidth()
                || this.xPos + this.width < other.getXPos()) {
            return false;
        }
        // checking y position bounds
        if (this.yPos > other.getYPos() + other.getHeight()
                || this.yPos + this.height < other.getYPos()) {
            return false;
        }
        return true;
    }


    /**
     * Returns random location in this room/on wall to start
     */
    private int randomWallPositionX(Random random) {
        //int x = this.xPos + random.nextInt(width/2) ;
        int x = this.xPos + this.width / 2;
        return x;
    }

    /**
     * Returns random Y position in this room
     */
    private int randomWallPositionY(Random random) {
        //int y = this.yPos + random.nextInt(height/2);
        int y = this.yPos + this.height / 2;

        return y;
    }

    /**
     * draws the hall blocks
     */
    private void drawHallBlock(TETile[][] world, int x, int y) {
        world[x][y] = FLOOR;
        if (world[x + 1][y] != FLOOR) {
            world[x + 1][y] = WALL;
        }
        if (world[x - 1][y] != FLOOR) {
            world[x - 1][y] = WALL;
        }
        if (world[x][y + 1] != FLOOR) {
            world[x][y + 1] = WALL;
        }
        if (world[x][y - 1] != FLOOR) {
            world[x][y - 1] = WALL;
        }
    }

    /**
     * Connects this room to given room, drawing hallway inbetween
     */
    public void connectTo(Room other, TETile[][] world, Random random) {
        // point in room we're at
        int startX = this.randomWallPositionX(random);
        int startY = this.randomWallPositionY(random);

        // point in room we want to get to
        int endX = other.randomWallPositionX(random);
        int endY = other.randomWallPositionY(random);

        //were our hallway is currently at
        int x = startX;
        int y = startY;

        // move where hallway is being drawn
        while (x != endX || y != endY) {
            if (x < endX) {
                x += 1;
            } else if (x > endX) {
                x -= 1;
            } else if (y < endY) {
                y += 1;
            } else if (y > endY) {
                y -= 1;
            }
            //world[x][y] = Tileset.FLOWER;
            drawHallBlock(world, x, y);

        }
    }
}


