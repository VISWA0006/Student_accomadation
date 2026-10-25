package com.accommodation.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "accommodation_stays")
public class AccommodationStay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "check_out_note", length = 1000)
    private String checkOutNote;

    protected AccommodationStay() {
    }

    public AccommodationStay(Booking booking) {
        this.booking = booking;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public LocalDateTime getCheckInAt() {
        return checkInAt;
    }

    public LocalDateTime getCheckOutAt() {
        return checkOutAt;
    }

    public String getCheckOutNote() {
        return checkOutNote;
    }

    public void checkIn() {
        if (checkInAt == null) {
            checkInAt = LocalDateTime.now();
        }
    }

    public void checkOut(String note) {
        if (checkInAt == null) {
            throw new IllegalStateException("The student must be checked in before checking out.");
        }
        if (checkOutAt == null) {
            checkOutAt = LocalDateTime.now();
            checkOutNote = note;
        }
    }
}
