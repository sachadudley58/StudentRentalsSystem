package studentrentals;

import studentrentals.model.*;
import studentrentals.search.SortByPriceAsc;
import studentrentals.search.SortByPriceDesc;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public final class Main {

    public static void main(String[] args) {
        StudentRentalsSystem system = new StudentRentalsSystem();
        system.seedAdminAndSampleData(); // helpful for demo/testing

        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("=== StudentRentals Prototype (Java 17) ===");

            while (true) {
                System.out.println("\nMain Menu");
                System.out.println("1) Register Student");
                System.out.println("2) Register Homeowner");
                System.out.println("3) Homeowner: Create Property");
                System.out.println("4) Homeowner: Add Room");
                System.out.println("5) Student: Search Rooms");
                System.out.println("6) Student: Request Booking");
                System.out.println("7) Homeowner: View Booking Requests");
                System.out.println("8) Homeowner: Accept/Reject Booking Request");
                System.out.println("9) Admin: View Users");
                System.out.println("10) Admin: View Listings");
                System.out.println("11) Admin: Deactivate User");
                System.out.println("12) Admin: Remove Listing (Room)");
                System.out.println("0) Exit");

                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> registerStudent(sc, system);
                        case "2" -> registerHomeowner(sc, system);
                        case "3" -> createProperty(sc, system);
                        case "4" -> addRoom(sc, system);
                        case "5" -> searchRooms(sc, system);
                        case "6" -> requestBooking(sc, system);
                        case "7" -> viewBookingRequests(sc, system);
                        case "8" -> decideBookingRequest(sc, system);
                        case "9" -> adminViewUsers(sc, system);
                        case "10" -> adminViewListings(sc, system);
                        case "11" -> adminDeactivateUser(sc, system);
                        case "12" -> adminRemoveListing(sc, system);
                        case "0" -> {
                            System.out.println("Bye!");
                            return;
                        }
                        default -> System.out.println("Invalid choice.");
                    }
                } catch (IllegalArgumentException ex) {
                    System.out.println("Error: " + ex.getMessage());
                } catch (Exception ex) {
                    System.out.println("Unexpected error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                }
            }
        }
    }

    private static void registerStudent(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Register Student (FR-01, FR-02) --");
        System.out.print("Name: "); String name = sc.nextLine().trim();
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Phone: "); String phone = sc.nextLine().trim();
        System.out.print("Password: "); String password = sc.nextLine();
        System.out.print("University Name: "); String uni = sc.nextLine().trim();
        System.out.print("Student ID: "); String sid = sc.nextLine().trim();

        Student s = system.registerStudent(name, email, phone, password, uni, sid);
        System.out.println("Registered student: " + s.getEmail());
    }

    private static void registerHomeowner(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Register Homeowner (FR-01) --");
        System.out.print("Name: "); String name = sc.nextLine().trim();
        System.out.print("Email: "); String email = sc.nextLine().trim();
        System.out.print("Phone: "); String phone = sc.nextLine().trim();
        System.out.print("Password: "); String password = sc.nextLine();

        Homeowner h = system.registerHomeowner(name, email, phone, password);
        System.out.println("Registered homeowner: " + h.getEmail());
    }

    private static void createProperty(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Create Property (FR-05) --");
        System.out.print("Owner email: "); String ownerEmail = sc.nextLine().trim();
        System.out.print("Address: "); String address = sc.nextLine().trim();
        System.out.print("Area/City: "); String area = sc.nextLine().trim();
        System.out.print("Description: "); String desc = sc.nextLine().trim();

        Property p = system.createProperty(ownerEmail, address, area, desc);
        System.out.println("Created property: " + p.getId());
    }

    private static void addRoom(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Add Room (FR-06) --");
        System.out.print("Owner email: "); String ownerEmail = sc.nextLine().trim();
        System.out.print("Property ID: "); UUID propertyId = UUID.fromString(sc.nextLine().trim());

        System.out.print("Room type (SINGLE/DOUBLE): ");
        RoomType type = RoomType.valueOf(sc.nextLine().trim().toUpperCase());

        System.out.print("Price per month (integer): ");
        int price = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Amenities (text): ");
        String amenities = sc.nextLine().trim();

        System.out.print("Available from (YYYY-MM-DD): ");
        LocalDate from = LocalDate.parse(sc.nextLine().trim());

        System.out.print("Available to (YYYY-MM-DD): ");
        LocalDate to = LocalDate.parse(sc.nextLine().trim());

        Room r = system.addRoom(ownerEmail, propertyId, type, price, amenities, from, to);
        System.out.println("Added room: " + r.getId());
    }

    private static void searchRooms(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Search Rooms (FR-09, FR-10, FR-11) --");
        SearchCriteria c = new SearchCriteria();

        System.out.print("Area/City (blank=any): ");
        c.setArea(blankToNull(sc.nextLine()));

        System.out.print("Min price (blank=any): ");
        String minP = sc.nextLine().trim();
        c.setMinPrice(minP.isBlank() ? null : Integer.parseInt(minP));

        System.out.print("Max price (blank=any): ");
        String maxP = sc.nextLine().trim();
        c.setMaxPrice(maxP.isBlank() ? null : Integer.parseInt(maxP));

        System.out.print("Room type SINGLE/DOUBLE (blank=any): ");
        String t = sc.nextLine().trim();
        c.setRoomType(t.isBlank() ? null : RoomType.valueOf(t.toUpperCase()));

        System.out.print("Start date (YYYY-MM-DD): ");
        c.setStartDate(LocalDate.parse(sc.nextLine().trim()));

        System.out.print("End date (YYYY-MM-DD): ");
        c.setEndDate(LocalDate.parse(sc.nextLine().trim()));

        System.out.print("Sort (1=Price Asc, 2=Price Desc): ");
        String s = sc.nextLine().trim();

        List<Room> results = system.searchRooms(
                c,
                "2".equals(s) ? new SortByPriceDesc() : new SortByPriceAsc()
        );

        if (results.isEmpty()) {
            System.out.println("No matches.");
            return;
        }

        System.out.println("Matches:");
        for (Room r : results) {
            Property p = system.getPropertyById(r.getPropertyId());
            System.out.printf("- Room %s | %s | £%d | %s | Property: %s (%s)%n",
                    r.getId(), r.getType(), r.getPricePerMonth(), p.getArea(), p.getId(), p.getAddress());
        }
    }

    private static void requestBooking(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Request Booking (FR-12) --");
        System.out.print("Student email: "); String studentEmail = sc.nextLine().trim();
        System.out.print("Room ID: "); UUID roomId = UUID.fromString(sc.nextLine().trim());
        System.out.print("Start date (YYYY-MM-DD): "); LocalDate start = LocalDate.parse(sc.nextLine().trim());
        System.out.print("End date (YYYY-MM-DD): "); LocalDate end = LocalDate.parse(sc.nextLine().trim());

        BookingRequest req = system.requestBooking(studentEmail, roomId, start, end);
        System.out.println("Created request: " + req.getId() + " (PENDING)");
    }

    private static void viewBookingRequests(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- View Booking Requests (FR-13) --");
        System.out.print("Owner email: "); String ownerEmail = sc.nextLine().trim();

        List<BookingRequest> reqs = system.listRequestsForOwner(ownerEmail);
        if (reqs.isEmpty()) {
            System.out.println("No requests found.");
            return;
        }

        for (BookingRequest r : reqs) {
            System.out.printf("- Request %s | room=%s | student=%s | %s to %s | %s%n",
                    r.getId(), r.getRoomId(), r.getStudentId(), r.getStartDate(), r.getEndDate(), r.getStatus());
        }
    }

    private static void decideBookingRequest(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Accept/Reject Request (FR-13, FR-14) --");
        System.out.print("Owner email: "); String ownerEmail = sc.nextLine().trim();
        System.out.print("Request ID: "); UUID reqId = UUID.fromString(sc.nextLine().trim());
        System.out.print("Accept? (y/n): "); boolean accept = sc.nextLine().trim().equalsIgnoreCase("y");

        Booking booking = system.decideRequest(ownerEmail, reqId, accept);
        if (accept) {
            System.out.println("Accepted. Booking confirmed: " + booking.getId());
        } else {
            System.out.println("Rejected request.");
        }
    }

    private static void adminViewUsers(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Admin View Users (FR-20) --");
        System.out.print("Admin email: "); String adminEmail = sc.nextLine().trim();
        List<User> users = system.adminViewUsers(adminEmail);
        for (User u : users) {
            System.out.printf("- %s | %s | active=%s | role=%s%n",
                    u.getEmail(), u.getName(), u.isActive(), u.getClass().getSimpleName());
        }
    }

    private static void adminViewListings(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Admin View Listings (FR-21) --");
        System.out.print("Admin email: "); String adminEmail = sc.nextLine().trim();
        List<Property> props = system.adminViewListings(adminEmail);
        for (Property p : props) {
            System.out.printf("- Property %s | %s | %s | rooms=%d%n",
                    p.getId(), p.getArea(), p.getAddress(), p.getRoomIds().size());
        }
    }

    private static void adminDeactivateUser(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Admin Deactivate User (FR-22) --");
        System.out.print("Admin email: "); String adminEmail = sc.nextLine().trim();
        System.out.print("User email to deactivate: "); String userEmail = sc.nextLine().trim();
        system.adminDeactivateUser(adminEmail, userEmail);
        System.out.println("Deactivated: " + userEmail);
    }

    private static void adminRemoveListing(Scanner sc, StudentRentalsSystem system) {
        System.out.println("\n-- Admin Remove Listing (Room) (FR-22) --");
        System.out.print("Admin email: "); String adminEmail = sc.nextLine().trim();
        System.out.print("Room ID to remove: "); UUID roomId = UUID.fromString(sc.nextLine().trim());
        system.adminRemoveListing(adminEmail, roomId);
        System.out.println("Removed room: " + roomId);
    }

    private static String blankToNull(String s) {
        String t = s.trim();
        return t.isBlank() ? null : t;
    }
}
