package com.accommodation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "active_student_id", unique = true)
    private Long activeStudentId;

    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull(message = "Room is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull(message = "Booking date is required")
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";

    public Booking() {
    }

    public Booking(Student student, Room room, LocalDate bookingDate, String status) {
        this.student = student;
        this.room = room;
        this.bookingDate = bookingDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        synchronizeActiveStudentId();
    }

    @PrePersist
    @PreUpdate
    private void synchronizeActiveStudentId() {
        activeStudentId = "ACTIVE".equalsIgnoreCase(status) && student != null ? student.getId() : null;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", student=" + (student != null ? student.getFullName() : null) +
                ", room=" + (room != null ? room.getRoomNumber() : null) +
                ", bookingDate=" + bookingDate +
                ", status='" + status + '\'' +
                '}';
    }
}
