package studentrentals.search;

import studentrentals.model.Room;

import java.util.ArrayList;
import java.util.List;

public final class SortByPriceDesc implements RoomSortStrategy {

    @Override
    public List<Room> sort(List<Room> rooms) {
        List<Room> sorted = new ArrayList<>(rooms);

        // selection-style sort by price (descending)
        for (int i = 0; i < sorted.size(); i++) {
            for (int j = i + 1; j < sorted.size(); j++) {
                Room roomA = sorted.get(i);
                Room roomB = sorted.get(j);

                if (roomB.getPricePerMonth() > roomA.getPricePerMonth()) {
                    // Swap
                    sorted.set(i, roomB);
                    sorted.set(j, roomA);
                }
            }
        }

        return sorted;
    }
}
