package com.accommodation.dto;

import com.accommodation.entity.FeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.math.BigDecimal;

public class HostelFeeRequestDTO {

    @NotNull(message = "Please select a student")
    private Long studentId;

    @NotBlank(message = "Hostel name is required")
    private String hostelName;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotNull(message = "Fee type is required")
    private FeeType feeType = FeeType.MONTHLY_HOSTEL_FEE;

    @NotNull(message = "Fee amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least ₹1.00")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most two decimal places")
    private BigDecimal totalAmount;

    @NotNull(message = "Payment due date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    public HostelFeeRequestDTO() {
        this.dueDate = LocalDate.now().plusDays(15);
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public void setFeeType(FeeType feeType) {
        this.feeType = feeType;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
