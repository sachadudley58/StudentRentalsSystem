package studentrentals.model;

import java.util.UUID;

public final class Administrator extends User {
    public Administrator(UUID id, String name, String email, String phone, String passwordHash, boolean active) {
        super(id, name, email, phone, passwordHash, active);
    }
}
