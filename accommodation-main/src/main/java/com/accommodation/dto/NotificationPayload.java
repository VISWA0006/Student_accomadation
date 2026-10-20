package com.accommodation.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationPayload {

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Student email is required")
    private String studentEmail;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Booking or fee status is required")
    private String bookingStatus;

    private String eventType = "BOOKING"; // BOOKING, UPCOMING_FEE, PAYMENT_DUE, OVERDUE_FEE, PAYMENT_SUCCESS, PARTIAL_PAYMENT, ADMIN_ALERT
    private String feeType;
    private Double amount;
    private Double remainingBalance;
    private String receiptNumber;
    private String message;

    public NotificationPayload() {
    }

    public NotificationPayload(String studentName, String studentEmail, String roomNumber, String bookingStatus) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.roomNumber = roomNumber;
        this.bookingStatus = bookingStatus;
        this.eventType = "BOOKING";
    }

    public NotificationPayload(String studentName, String studentEmail, String roomNumber,
                               String bookingStatus, String eventType, String feeType,
                               Double amount, Double remainingBalance, String receiptNumber, String message) {
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.roomNumber = roomNumber;
        this.bookingStatus = bookingStatus;
        this.eventType = eventType;
        this.feeType = feeType;
        this.amount = amount;
        this.remainingBalance = remainingBalance;
        this.receiptNumber = receiptNumber;
        this.message = message;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
