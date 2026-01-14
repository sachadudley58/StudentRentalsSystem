package studentrentals;

import studentrentals.model.*;
import studentrentals.search.RoomSortStrategy;
import studentrentals.util.PasswordHasher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public final class StudentRentalsSystem {

    // using UML 
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<UUID, Property> propertiesById = new HashMap<>();
    private final Map<UUID, Room> roomsById = new HashMap<>();
    private final Map<UUID, BookingRequest> requestsById = new HashMap<>();
    private final Map<UUID, Booking> bookingsById = new HashMap<>();

    // for search (matches UML)
    private final Map<String, List<UUID>> roomsByArea = new HashMap<>();
    private final Map<RoomType, List<UUID>> roomsByType = new HashMap<>();

    public Student registerStudent(String name, String email, String phone, String password, String university, String studentId) {
        requireNonBlank(name, "Name");
        requireNonBlank(email, "Email");
        requireNonBlank(phone, "Phone");
        requireNonBlank(password, "Password");
        requireNonBlank(university, "University");
        requireNonBlank(studentId, "Student ID");

        String key = email.toLowerCase(Locale.ROOT);
        if (usersByEmail.containsKey(key)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        String hash = PasswordHasher.hashPassword(password);
        Student s = new Student(UUID.randomUUID(), name, key, phone, hash, true, university, studentId);
        usersByEmail.put(key, s);
        return s;
    }

    public Homeowner registerHomeowner(String name, String email, String phone, String password) {
        requireNonBlank(name, "Name");
        requireNonBlank(email, "Email");
        requireNonBlank(phone, "Phone");
        requireNonBlank(password, "Password");

        String key = email.toLowerCase(Locale.ROOT);
        if (usersByEmail.containsKey(key)) {
            throw new IllegalArgumentException("Email already registered.");
        }

        String hash = PasswordHasher.hashPassword(password);
        Homeowner h = new Homeowner(UUID.randomUUID(), name, key, phone, hash, true);
        usersByEmail.put(key, h);
        return h;
    }

    public Property createProperty(String ownerEmail, String address, String area, String description) {
        Homeowner owner = requireActiveHomeowner(ownerEmail);

        requireNonBlank(address, "Address");
        requireNonBlank(area, "Area");
        requireNonBlank(description, "Description");

        UUID id = UUID.randomUUID();
        Property p = new Property(id, owner.getId(), address, area, description);
        propertiesById.put(id, p);
        return p;
    }

    public Room addRoom(String ownerEmail, UUID propertyId, RoomType type, int pricePerMonth,
                        String amenities, LocalDate availableFrom, LocalDate availableTo) {
        Homeowner owner = requireActiveHomeowner(ownerEmail);
        Property p = propertiesById.get(propertyId);
        if (p == null) throw new IllegalArgumentException("Property not found.");
        if (!p.getOwnerId().equals(owner.getId())) throw new IllegalArgumentException("Not your property.");

        if (type == null) throw new IllegalArgumentException("Room type required.");
        if (pricePerMonth <= 0) throw new IllegalArgumentException("Price must be > 0.");
        requireNonBlank(amenities, "Amenities");
        requireDateRange(availableFrom, availableTo);

        UUID roomId = UUID.randomUUID();
        Room r = new Room(roomId, propertyId, owner.getId(), type, pricePerMonth, amenities, availableFrom, availableTo);
        roomsById.put(roomId, r);

        // Update property composition
        p.getRoomIds().add(roomId);

        // Update indexes
        roomsByArea.computeIfAbsent(p.getArea().toLowerCase(Locale.ROOT), k -> new ArrayList<>()).add(roomId);
        roomsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(roomId);

        return r;
    }

    public void updateRoom(String ownerEmail, UUID roomId, RoomType type, Integer pricePerMonth,
                           String amenities, LocalDate availableFrom, LocalDate availableTo) {
        Homeowner owner = requireActiveHomeowner(ownerEmail);
        Room r = roomsById.get(roomId);
        if (r == null) throw new IllegalArgumentException("Room not found.");
        if (!r.getOwnerId().equals(owner.getId())) throw new IllegalArgumentException("Not your room.");

        // Minimal update logic (keep consistent with fields)
        if (type != null && type != r.getType()) {
            // update type index
            List<UUID> oldList = roomsByType.getOrDefault(r.getType(), new ArrayList<>());
            oldList.remove(roomId);
            roomsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(roomId);
            r.setType(type);
        }
        if (pricePerMonth != null) {
            if (pricePerMonth <= 0) throw new IllegalArgumentException("Price must be > 0.");
            r.setPricePerMonth(pricePerMonth);
        }
        if (amenities != null && !amenities.isBlank()) {
            r.setAmenities(amenities.trim());
        }
        if (availableFrom != null || availableTo != null) {
            LocalDate from = (availableFrom != null) ? availableFrom : r.getAvailableFrom();
            LocalDate to = (availableTo != null) ? availableTo : r.getAvailableTo();
            requireDateRange(from, to);
            r.setAvailableFrom(from);
            r.setAvailableTo(to);
        }
    }

    public void removeRoom(String ownerEmail, UUID roomId) {
        Homeowner owner = requireActiveHomeowner(ownerEmail);
        Room r = roomsById.get(roomId);
        if (r == null) throw new IllegalArgumentException("Room not found.");
        if (!r.getOwnerId().equals(owner.getId())) throw new IllegalArgumentException("Not your room.");

        // Prototype rule: allow removal only if no confirmed bookings exist
        if (!r.getConfirmedBookings().isEmpty()) {
            throw new IllegalArgumentException("Cannot remove room with confirmed bookings.");
        }
        internalRemoveRoom(roomId);
    }

    public List<Room> searchRooms(SearchCriteria criteria, RoomSortStrategy sortStrategy) {
        if (criteria == null) {
            throw new IllegalArgumentException("Search criteria must not be null.");
        }
        if (sortStrategy == null) {
            throw new IllegalArgumentException("Sort strategy must not be null.");
        }

        // Validate required date range
        requireDateRange(criteria.getStartDate(), criteria.getEndDate());

        // Step 1: determine candidate room IDs using simple indexing
        Set<UUID> candidateRoomIds = new HashSet<>();
        boolean usedIndex = false;

        // Filter by area, if provided
        if (criteria.getArea() != null) {
            String areaKey = criteria.getArea().trim().toLowerCase(Locale.ROOT);
            List<UUID> roomsInArea = roomsByArea.get(areaKey);
            if (roomsInArea != null) {
                candidateRoomIds.addAll(roomsInArea);
            }
            usedIndex = true;
        }

        // Filter by room type if provided
        if (criteria.getRoomType() != null) {
            List<UUID> roomsOfType = roomsByType.get(criteria.getRoomType());
            if (roomsOfType == null) {
                roomsOfType = new ArrayList<>();
            }

            if (!usedIndex) {
                candidateRoomIds.addAll(roomsOfType);
                usedIndex = true;
            } else {
                // Intersect existing candidates with room-type matches
                Set<UUID> intersection = new HashSet<>();
                for (UUID id : roomsOfType) {
                    if (candidateRoomIds.contains(id)) {
                        intersection.add(id);
                    }
                }
                candidateRoomIds = intersection;
            }
        }

        // If no index was used, fall back to all rooms
        if (!usedIndex) {
            candidateRoomIds.addAll(roomsById.keySet());
        }

        // step 2: apply detailed filtering
        List<Room> matchingRooms = new ArrayList<>();

        for (UUID roomId : candidateRoomIds) {
            Room room = roomsById.get(roomId);
            if (room == null) {
                continue;
            }

            // Area check (safe even if indexed)
            if (criteria.getArea() != null) {
                Property property = propertiesById.get(room.getPropertyId());
                if (property == null) {
                    continue;
                }
                if (!property.getArea().equalsIgnoreCase(criteria.getArea().trim())) {
                    continue;
                }
            }

            // Room type check
            if (criteria.getRoomType() != null && room.getType() != criteria.getRoomType()) {
                continue;
            }

            // Price checks
            if (criteria.getMinPrice() != null && room.getPricePerMonth() < criteria.getMinPrice()) {
                continue;
            }
            if (criteria.getMaxPrice() != null && room.getPricePerMonth() > criteria.getMaxPrice()) {
                continue;
            }

            // Availability window check
            if (!room.isWithinWindow(criteria.getStartDate(), criteria.getEndDate())) {
                continue;
            }

            // Overlap check against confirmed bookings
            if (!room.isAvailable(criteria.getStartDate(), criteria.getEndDate())) {
                continue;
            }

            matchingRooms.add(room);
        }

        // Step 3: sort results using the chosen strategy
        return sortStrategy.sort(matchingRooms);
    }


    public Room getRoomDetails(UUID roomId) {
        Room r = roomsById.get(roomId);
        if (r == null)
        {
             throw new IllegalArgumentException("Room not found.");
        }
        return r;
    }

    public Property getPropertyById(UUID propertyId) {
        Property p = propertiesById.get(propertyId);
        if (p == null) throw new IllegalArgumentException("Property not found.");
        return p;
    }

    public BookingRequest requestBooking(String studentEmail, UUID roomId, LocalDate start, LocalDate end) {
        Student student = requireActiveStudent(studentEmail);
        Room room = roomsById.get(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room not found.");
        }

        requireDateRange(start, end);

        if (!room.isWithinWindow(start, end)) {
            throw new IllegalArgumentException("Requested dates are outside the room's availability window.");
        }
        if (!room.isAvailable(start, end)) {
            throw new IllegalArgumentException("Room is not available for that date range.");
        }

        BookingRequest req = new BookingRequest(
                UUID.randomUUID(),
                student.getId(),
                roomId,
                start,
                end,
                RequestStatus.PENDING,
                LocalDateTime.now()
        );

        requestsById.put(req.getId(), req);
        return req;
    }

    public List<BookingRequest> listRequestsForOwner(String ownerEmail) {
        Homeowner owner = requireActiveHomeowner(ownerEmail);

        // find room IDs owned by this homeowner
        Set<UUID> ownerRoomIds = new HashSet<>();
        for (Room r : roomsById.values()) {
            if (r.getOwnerId().equals(owner.getId())) {
                ownerRoomIds.add(r.getId());
            }
        }

        // collect booking requests for those rooms
        List<BookingRequest> results = new ArrayList<>();
        for (BookingRequest req : requestsById.values()) {
            if (ownerRoomIds.contains(req.getRoomId())) {
                results.add(req);
            }
        }

        // sort requests by creation time (oldest first)
        for (int i = 0; i < results.size(); i++) {
            for (int j = i + 1; j < results.size(); j++) {
                BookingRequest a = results.get(i);
                BookingRequest b = results.get(j);

                if (a.getCreatedAt().isAfter(b.getCreatedAt())) {
                    // Swap positions
                    results.set(i, b);
                    results.set(j, a);
                }
            }
        }

        return results;
    }



    public Booking decideRequest(String ownerEmail, UUID requestId, boolean accept) {
        Homeowner owner = requireActiveHomeowner(ownerEmail);

        BookingRequest req = requestsById.get(requestId);
        if (req == null) throw new IllegalArgumentException("Request not found.");
        if (req.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not pending.");
        }

        Room room = roomsById.get(req.getRoomId());
        if (room == null) throw new IllegalArgumentException("Room not found.");
        if (!room.getOwnerId().equals(owner.getId())) throw new IllegalArgumentException("Not your room.");

        if (!accept) {
            req.markRejected();
            return null; // if rejected, no booking returned
        }

        //important requirement: re-check overlap before confirming (FR-14)
        if (!room.isWithinWindow(req.getStartDate(), req.getEndDate())) {
            req.markRejected();
            throw new IllegalArgumentException("Request dates no longer within availability window.");
        }
        if (!room.isAvailable(req.getStartDate(), req.getEndDate())) {
            req.markRejected();
            throw new IllegalArgumentException("Conflict detected: room already booked for those dates.");
        }

        Booking booking = new Booking(
                UUID.randomUUID(),
                req.getStudentId(),
                req.getRoomId(),
                req.getStartDate(),
                req.getEndDate(),
                LocalDateTime.now()
        );

        bookingsById.put(booking.getId(), booking);
        room.addConfirmedBooking(booking);
        req.markAccepted();

        return booking;
    }

    public List<User> adminViewUsers(String adminEmail) {
        requireAdmin(adminEmail);

        List<User> users = new ArrayList<>();
        for (User u : usersByEmail.values()) {
            users.add(u);
        }

        users.sort(new Comparator<User>() {
            @Override
            public int compare(User a, User b) {
                return a.getEmail().compareTo(b.getEmail());
            }
        });

        return users;
    }


    public List<Property> adminViewListings(String adminEmail) {
        requireAdmin(adminEmail);

        List<Property> props = new ArrayList<>();
        for (Property p : propertiesById.values()) {
            props.add(p);
        }

        props.sort(new Comparator<Property>() {
            @Override
            public int compare(Property a, Property b) {
                int areaCompare = a.getArea().compareToIgnoreCase(b.getArea());
                if (areaCompare != 0) {
                    return areaCompare;
                }
                return a.getAddress().compareToIgnoreCase(b.getAddress());
            }
        });

        return props;
    }


    public void adminDeactivateUser(String adminEmail, String userEmail) {
        requireAdmin(adminEmail);
        requireNonBlank(userEmail, "User email");

        User u = usersByEmail.get(userEmail.toLowerCase(Locale.ROOT));
        if (u == null) throw new IllegalArgumentException("User not found.");
        u.deactivate();
    }

    public void adminRemoveListing(String adminEmail, UUID roomId) {
        requireAdmin(adminEmail);

        Room r = roomsById.get(roomId);
        if (r == null) throw new IllegalArgumentException("Room not found.");

        // Prototype rule: allow admin removal even if bookings exist (admin override)
        internalRemoveRoom(roomId);
    }

    private void internalRemoveRoom(UUID roomId) {
        Room r = roomsById.remove(roomId);
        if (r == null) return;

        Property p = propertiesById.get(r.getPropertyId());
        if (p != null) {
            p.getRoomIds().remove(roomId);
            // update area index using property area
            String areaKey = p.getArea().toLowerCase(Locale.ROOT);
            roomsByArea.getOrDefault(areaKey, new ArrayList<>()).remove(roomId);
        }

        roomsByType.getOrDefault(r.getType(), new ArrayList<>()).remove(roomId);

        // Remove pending requests for this room
        requestsById.values().removeIf(req -> req.getRoomId().equals(roomId));
    }

    private boolean isRoomSearchMatch(Room r, SearchCriteria c) {
        // Area filter: already indexed if provided, but still safe if not
        if (c.getArea() != null) {
            Property p = propertiesById.get(r.getPropertyId());
            if (p == null) return false;
            if (!p.getArea().equalsIgnoreCase(c.getArea().trim())) return false;
        }
        if (c.getRoomType() != null && r.getType() != c.getRoomType()) return false;
        if (c.getMinPrice() != null && r.getPricePerMonth() < c.getMinPrice()) return false;
        if (c.getMaxPrice() != null && r.getPricePerMonth() > c.getMaxPrice()) return false;

        if (!r.isWithinWindow(c.getStartDate(), c.getEndDate())) return false;
        return r.isAvailable(c.getStartDate(), c.getEndDate());
    }

    private Student requireActiveStudent(String email) {
        User u = requireUser(email);
        if (!(u instanceof Student s)) throw new IllegalArgumentException("User is not a student.");
        if (!s.isActive()) throw new IllegalArgumentException("Student account is deactivated.");
        return s;
    }

    private Homeowner requireActiveHomeowner(String email) {
        User u = requireUser(email);
        if (!(u instanceof Homeowner h)) throw new IllegalArgumentException("User is not a homeowner.");
        if (!h.isActive()) throw new IllegalArgumentException("Homeowner account is deactivated.");
        return h;
    }

    private void requireAdmin(String email) {
        User u = requireUser(email);
        if (!(u instanceof Administrator)) throw new IllegalArgumentException("User is not an administrator.");
        if (!u.isActive()) throw new IllegalArgumentException("Admin account is deactivated.");
    }

    private User requireUser(String email) {
        requireNonBlank(email, "Email");
        User u = usersByEmail.get(email.toLowerCase(Locale.ROOT));
        if (u == null) throw new IllegalArgumentException("User not found.");
        return u;
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    private static void requireDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) throw new IllegalArgumentException("Start and end dates are required.");
        if (!start.isBefore(end)) throw new IllegalArgumentException("Start date must be before end date.");
    }

    // Helpful for testing and demos; not part of FR list but fine for prototype.
    public void seedAdminAndSampleData() {
        // Admin (FR-20/21/22)
        String adminEmail = "admin@studentrentals.com";
        if (!usersByEmail.containsKey(adminEmail)) {
            Administrator admin = new Administrator(
                    UUID.randomUUID(),
                    "Admin",
                    adminEmail,
                    "00000000000",
                    PasswordHasher.hashPassword("admin123"),
                    true
            );
            usersByEmail.put(adminEmail, admin);
        }

        // Seed a homeowner + property + rooms
        if (!usersByEmail.containsKey("owner@example.com")) {
            registerHomeowner("Test Owner", "owner@example.com", "07123456789", "pass");
            Property p = createProperty("owner@example.com", "1 High Street", "Cardiff", "Near campus");
            addRoom("owner@example.com", p.getId(), RoomType.SINGLE, 550, "WiFi, Desk",
                    LocalDate.now().plusDays(1), LocalDate.now().plusMonths(6));
            addRoom("owner@example.com", p.getId(), RoomType.DOUBLE, 700, "WiFi, Private bathroom",
                    LocalDate.now().plusDays(10), LocalDate.now().plusMonths(8));
        }

        // Seed a student
        if (!usersByEmail.containsKey("student@example.com")) {
            registerStudent("Test Student", "student@example.com", "07999999999", "pass",
                    "Cardiff University", "C1234567");
        }
    }
}
