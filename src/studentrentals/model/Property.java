package studentrentals.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Property {
    private final UUID id;
    private final UUID ownerId;
    private final String address;
    private final String area;
    private final String description;
    private final List<UUID> roomIds = new ArrayList<>();

    public Property(UUID id, UUID ownerId, String address, String area, String description) {
        this.id = id;
        this.ownerId = ownerId;
        this.address = address;
        this.area = area;
        this.description = description;
    }

    public UUID getId() { return id; }
    public UUID getOwnerId() { return ownerId; }
    public String getAddress() { return address; }
    public String getArea() { return area; }
    public String getDescription() { return description; }
    public List<UUID> getRoomIds() { return roomIds; }
}
