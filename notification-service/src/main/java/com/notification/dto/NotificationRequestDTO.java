package com.notification.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationRequestDTO {

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Student email is required")
    private String studentEmail;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Booking status is required")
    private String bookingStatus;

    public NotificationRequestDTO() {
    }

    public NotificationRequestDTO(String studentName, String studentEmail, String roomNumber, String bookingStatus) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.roomNumber = roomNumber;
        this.bookingStatus = bookingStatus;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}
