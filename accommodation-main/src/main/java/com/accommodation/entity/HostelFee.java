package com.accommodation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Student Hostel Fee record.
 * Mapped to 'hostel_fees' table in PostgreSQL / H2.
 */
@Entity
@Table(name = "hostel_fees")
public class HostelFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Student is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotBlank(message = "Hostel name is required")
    @Column(name = "hostel_name", nullable = false)
    private String hostelName;

    @NotBlank(message = "Room number is required")
    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @NotNull(message = "Fee type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false)
    private FeeType feeType = FeeType.MONTHLY_HOSTEL_FEE;

    @NotNull(message = "Total fee amount is required")
    @DecimalMin(value = "0.01", message = "Fee amount must be greater than zero")
    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @NotNull
    @Column(name = "paid_amount", nullable = false)
    private Double paidAmount = 0.0;

    @NotNull
    @Column(name = "remaining_balance", nullable = false)
    private Double remainingBalance;

    @NotNull(message = "Payment due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "transaction_reference")
    private String transactionReference;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @OneToMany(mappedBy = "hostelFee", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("paymentDate DESC")
    private List<PaymentTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "hostelFee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InAppNotification> notifications = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public HostelFee() {
    }

    public HostelFee(Student student, String hostelName, String roomNumber, FeeType feeType,
                     Double totalAmount, LocalDate dueDate) {
        this.student = student;
        this.hostelName = hostelName;
        this.roomNumber = roomNumber;
        this.feeType = feeType;
        this.totalAmount = totalAmount;
        this.paidAmount = 0.0;
        this.remainingBalance = totalAmount;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        recalculateStatus(LocalDate.now());
    }

    /**
     * Recomputes remaining balance and payment status strictly according to business rules:
     * 1. Balance = Total - Paid
     * 2. If balance <= 0 (or paid == total) -> PAID (A fully paid fee must NEVER be marked overdue)
     * 3. If balance > 0 and dueDate < currentDate -> OVERDUE (Partially paid or unpaid past due date)
     * 4. If balance > 0 and dueDate == currentDate -> UPCOMING or PENDING due today
     * 5. If balance > 0 and dueDate is within next 7 days -> UPCOMING
     * 6. If balance > 0 and dueDate > 7 days away -> PARTIALLY_PAID if paid > 0 else PENDING
     */
    public void recalculateStatus(LocalDate currentDate) {
        if (currentDate == null) {
            currentDate = LocalDate.now();
        }
        if (this.totalAmount == null) {
            this.totalAmount = 0.0;
        }
        if (this.paidAmount == null) {
            this.paidAmount = 0.0;
        }

        // Remaining balance calculation rounded to 2 decimal places
        double balance = Math.round((this.totalAmount - this.paidAmount) * 100.0) / 100.0;
        if (balance < 0.001) {
            balance = 0.0;
        }
        this.remainingBalance = balance;

        if (this.remainingBalance <= 0.0) {
            this.status = PaymentStatus.PAID;
            return;
        }

        // Unpaid or partially paid balance remains
        if (this.dueDate.isBefore(currentDate)) {
            // Due date has passed and balance remains unpaid
            this.status = PaymentStatus.OVERDUE;
        } else if (!this.dueDate.isAfter(currentDate.plusDays(7))) {
            // Due date is approaching (within next 7 days or today) and balance remains unpaid
            this.status = PaymentStatus.UPCOMING;
        } else if (this.paidAmount > 0.0) {
            // Paid partially, but due date is not yet within 7 days
            this.status = PaymentStatus.PARTIALLY_PAID;
        } else {
            // No payment recorded, due date is comfortably in future
            this.status = PaymentStatus.PENDING;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Returns how many days overdue this record is (0 if not overdue).
     */
    public long getDaysOverdue(LocalDate currentDate) {
        if (currentDate == null) currentDate = LocalDate.now();
        if (this.dueDate != null && this.dueDate.isBefore(currentDate) && this.remainingBalance != null && this.remainingBalance > 0.0) {
            return ChronoUnit.DAYS.between(this.dueDate, currentDate);
        }
        return 0;
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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public List<PaymentTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<PaymentTransaction> transactions) {
        this.transactions = transactions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
