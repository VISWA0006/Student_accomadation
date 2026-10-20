package com.accommodation.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class BookingRequestDTO {

    @NotNull(message = "Please select a student")
    private Long studentId;

    @NotNull(message = "Please select a room")
    private Long roomId;

    @NotNull(message = "Booking date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate bookingDate = LocalDate.now();

    public BookingRequestDTO() {
    }

    public BookingRequestDTO(Long studentId, Long roomId, LocalDate bookingDate) {
        this.studentId = studentId;
        this.roomId = roomId;
        this.bookingDate = bookingDate;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
}
