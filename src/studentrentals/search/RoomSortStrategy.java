package studentrentals.search;

import studentrentals.model.Room;

import java.util.List;

public interface RoomSortStrategy {
    List<Room> sort(List<Room> rooms);
}
