package studentrentals.model;

import java.util.UUID;

public final class Homeowner extends User {
    public Homeowner(UUID id, String name, String email, String phone, String passwordHash, boolean active) {
        super(id, name, email, phone, passwordHash, active);
    }
}
