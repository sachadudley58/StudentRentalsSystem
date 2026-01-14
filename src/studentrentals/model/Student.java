package studentrentals.model;

import java.util.UUID;

public final class Student extends User {
    private final String universityName;
    private final String studentId;

    public Student(UUID id, String name, String email, String phone, String passwordHash, boolean active, String universityName, String studentId) {
        super(id, name, email, phone, passwordHash, active);
        this.universityName = universityName;
        this.studentId = studentId;
    }

    public String getUniversityName() { return universityName; }

    
    public String getStudentId() { return studentId; }
}
