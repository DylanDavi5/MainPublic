package byow.Core;

import byow.TileEngine.TETile;
import org.junit.Test;

import static org.junit.Assert.*;


public class EngineTests {
    /*
    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing aList = new AListNoResizing();
        BuggyAList bList = new BuggyAList();
        aList.addLast(4);
        bList.addLast(4);
        aList.addLast(5);
        bList.addLast(5);
        aList.addLast(6);
        bList.addLast(6);

        assertEquals(aList.size(), bList.size());
        assertEquals(aList.removeLast(), bList.removeLast());
        assertEquals(aList.removeLast(), bList.removeLast());
        assertEquals(aList.removeLast(), bList.removeLast());

    }

    @Test
    public void inputStringTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                broken.addLast(randVal);
                //System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int expected = correct.size();
                int actual = broken.size();
                assertEquals(expected, actual);
                //System.out.println("size: " + size);
            } else if (operationNumber == 2){
                if (correct.size() > 0) {
                    int expected = correct.getLast();
                    int actual = broken.getLast();
                    assertEquals(expected, actual);
                }
            } else if (operationNumber == 3){
                if (correct.size() > 0){
                    int expected = correct.removeLast();
                    int actual = broken.removeLast();
                    assertEquals(expected, actual);
                }

            }
        }
    }

     */
    @Test
    public void inputStringTest() {
        Engine engine = new Engine();
        String input1 = "n731345sasdw";
        //String input2 = "n7313sasdw";
        String input2 = "n73134542342sasdwsssss";

        TETile[][] world1 = engine.interactWithInputString(input1);
        TETile[][] world2 = engine.interactWithInputString(input2);
        assertFalse(worldsEqual(world1, world2));

    }

    /**
     * Returns whether all tiles in world1 equal all tiles in world2
     */
    private static boolean worldsEqual(TETile[][] world1, TETile[][] world2) {
        // world must be same size
        if (world1.length != world1.length || world1[0].length != world2[0].length) {
            return false;
        }
        // all tiles in world must be the same
        for (int x = 0; x < world1.length; x += 1) {
            for (int y = 0; y < world1[0].length; y += 1) {
                if (world1[x][y].equals(world2[x][y])) {
                    return false;
                }
            }
        }
        return true;
    }
    // YOUR TESTS HERE
}
