package studentrentals.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class Booking {
    private final UUID id;
    private final UUID studentId;
    private final UUID roomId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime confirmedAt;

    public Booking(UUID id, UUID studentId, UUID roomId, LocalDate startDate, LocalDate endDate, LocalDateTime confirmedAt) 
    {
        this.id = id;
        this.studentId = studentId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.confirmedAt = confirmedAt;
    }

    public UUID getId() 
    { 
        return id; 
    }
    
    public UUID getStudentId() 
    { 
        return studentId; 
    }
    
    public UUID getRoomId() 
    { 
        return roomId; 
    }
    
    public LocalDate getStartDate() 
    { 
        return startDate; 
    }
    
    public LocalDate getEndDate() 
    { 
        return endDate; 
    }
    
    public LocalDateTime getConfirmedAt() 
    { 
        return confirmedAt; 
    }
}
