package studentrentals.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public final class BookingRequest {
    private final UUID id;
    private final UUID studentId;
    private final UUID roomId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private RequestStatus status;
    private final LocalDateTime createdAt;

    public BookingRequest(UUID id, UUID studentId, UUID roomId, LocalDate startDate, LocalDate endDate, RequestStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.roomId = roomId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }

    public UUID getStudentId() { return studentId; }

    public UUID getRoomId() { return roomId; }

    public LocalDate getStartDate() { return startDate; }

    public LocalDate getEndDate() { return endDate; }

    public RequestStatus getStatus() { return status; }

    public LocalDateTime getCreatedAt() { return createdAt; }



    public void markAccepted() { this.status = RequestStatus.ACCEPTED; }


    public void markRejected() { this.status = RequestStatus.REJECTED; }
}
