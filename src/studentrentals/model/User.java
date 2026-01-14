package studentrentals.model;

import java.util.UUID;

public abstract class User {
    private final UUID id;
    private final String name;
    private final String email;
    private final String phone;
    private final String passwordHash;
    private boolean active;

    protected User(UUID id, String name, String email, String phone, String passwordHash, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    public UUID getId() { return id; }
    
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }



    public void deactivate() { this.active = false; }

    public boolean isActive() { return active; }
}
