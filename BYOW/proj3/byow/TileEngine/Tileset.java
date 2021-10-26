package byow.TileEngine;

import java.awt.Color;
import java.io.File;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 * <p>
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 * <p>
 * Ex:
 * world[x][y] = Tileset.FLOOR;
 * <p>
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    public static final File IMAGE_DIR = imageDir();


    public static final TETile AVATAR = new TETile('@', Color.white, Color.black, "you");
    public static final File AVATAR_UP_TEXTURE = new File(IMAGE_DIR, "characterWalkingUp.png");
    public static final File AVATAR_DOWN_TEXTURE = new File(IMAGE_DIR, "characterWalkingDown.png");
    public static final File AVATAR_LEFT_TEXTURE = new File(IMAGE_DIR, "characterWalkingLeft.png");
    public static final File AVATAR_RIGHT_TEXTURE =
            new File(IMAGE_DIR, "characterWalkingRight.png");
    public static final TETile AVATAR_UP = new TETile('@', Color.white, Color.BLACK, "you",
            AVATAR_UP_TEXTURE.getPath());
    public static final TETile AVATAR_DOWN = new TETile('@', Color.white, Color.BLACK, "you",
            AVATAR_DOWN_TEXTURE.getPath());
    public static final TETile AVATAR_LEFT = new TETile('@', Color.white, Color.BLACK, "you",
            AVATAR_LEFT_TEXTURE.getPath());
    public static final TETile AVATAR_RIGHT = new TETile('@', Color.white, Color.BLACK, "you",
            AVATAR_RIGHT_TEXTURE.getPath());

    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "wall");
    public static final File BRICK_TEXTURE_FILE = new File(IMAGE_DIR, "brickWall.png");
    public static final TETile BRICK_WALL = new TETile('#', new Color(216, 128, 128),
            Color.darkGray, "wall", BRICK_TEXTURE_FILE.getPath());


    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black,
            "floor");
    public static final File WOOD_TEXTURE_FILE = new File(IMAGE_DIR, "woodFloor.png");
    public static final TETile WOOD_FLOOR = new TETile('·', new Color(128, 192, 128), Color.black,
            "floor", WOOD_TEXTURE_FILE.getPath());

    public static final File TORCH_TEXTURE_FILE = new File(IMAGE_DIR, "torch.png");
    public static final File TORCH_WOOD_BACKGROUND_TEXTURE_FILE = new
            File(IMAGE_DIR, "torchWoodBackground.png");
    public static final TETile TORCH = new TETile('!', Color.YELLOW, Color.black,
            "A Torch!", TORCH_WOOD_BACKGROUND_TEXTURE_FILE.getPath());

    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "nothing");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "grass");
    public static final TETile WATER = new TETile('≈', Color.blue, Color.black, "water");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "flower");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "locked door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "sand");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "mountain");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "tree");

    /**
     * Returns description corresponding to given tile
     */
    public static void getDescription(TETile tile) {
        //switch case then check

    }

    private static File imageDir() {
        File proj3 = new File(System.getProperty("user.dir"));
        if (!proj3.getName().equals("proj3")) {
            //in case CWD is the sp21 folder
            proj3 = new File(proj3, "proj3");
        }
        File byow = new File(proj3, "byow");
        File tileEngine = new File(byow, "TileEngine");
        File imageDir = new File(tileEngine, "tileImages");
        //System.out.println(imageDir.getPath());
        return imageDir;

    }

}


