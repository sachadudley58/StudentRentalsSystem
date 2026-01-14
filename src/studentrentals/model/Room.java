package studentrentals.model;

import studentrentals.util.DateRangeUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class Room {
    private final UUID id;
    private final UUID propertyId;
    private final UUID ownerId;
    private RoomType type;
    private int pricePerMonth;
    private String amenities;
    private LocalDate availableFrom;
    private LocalDate availableTo;

    // for FR-14 (double-book prevention)
    private final List<Booking> confirmedBookings = new ArrayList<>();

    public Room(UUID id, UUID propertyId, UUID ownerId, RoomType type, int pricePerMonth, String amenities, LocalDate availableFrom, LocalDate availableTo) {
        this.id = id;
        this.propertyId = propertyId;
        this.ownerId = ownerId;
        this.type = type;
        this.pricePerMonth = pricePerMonth;
        this.amenities = amenities;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }

    public UUID getId() { return id; }
    public UUID getPropertyId() { return propertyId; }
    public UUID getOwnerId() { return ownerId; }
    public RoomType getType() { return type; }
    public int getPricePerMonth() { return pricePerMonth; }
    public String getAmenities() { return amenities; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public LocalDate getAvailableTo() { return availableTo; }
    public List<Booking> getConfirmedBookings() { return confirmedBookings; }

    public void setType(RoomType type) { this.type = type; }
    public void setPricePerMonth(int pricePerMonth) { this.pricePerMonth = pricePerMonth; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }
    public void setAvailableTo(LocalDate availableTo) { this.availableTo = availableTo; }


    public boolean isWithinWindow(LocalDate start, LocalDate end) {
        //treat end as exclusive in overlap checks, but within-window means:
        // start >= availableFrom AND end <= availableTo AND start < end
        return (start != null && end != null) && !start.isBefore(availableFrom) && !end.isAfter(availableTo) && start.isBefore(end);
    }


    public boolean isAvailable(LocalDate start, LocalDate end) {
        for (Booking b : confirmedBookings) {
            if (DateRangeUtil.overlaps(start, end, b.getStartDate(), b.getEndDate())) {
                return false;
            }
        }
        return true;
    }

    public void addConfirmedBooking(Booking b) {
        confirmedBookings.add(b);
    }
}
