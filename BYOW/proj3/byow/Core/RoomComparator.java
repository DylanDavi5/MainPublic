package byow.Core;

import java.util.Comparator;

public class RoomComparator implements Comparator<Room> {
    @Override
    /** Compare x positions of rooms to facilitate certain hallway connections*/
    public int compare(Room r1, Room r2) {
        int x1 = r1.getXPos();
        int x2 = r2.getXPos();

        if (x1 == x2) {
            return 0;
        } else if (x1 > x2) {
            return 1;
        } else {
            return -1;
        }
    }
}
