package com.accommodation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
    @Digits(integer = 10, fraction = 2, message = "Fee amount must have at most two decimal places")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @NotNull
    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @NotNull
    @Column(name = "remaining_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal remainingBalance;

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
        this(student, hostelName, roomNumber, feeType, BigDecimal.valueOf(totalAmount), dueDate);
    }

    public HostelFee(Student student, String hostelName, String roomNumber, FeeType feeType,
                     BigDecimal totalAmount, LocalDate dueDate) {
        this.student = student;
        this.hostelName = hostelName;
        this.roomNumber = roomNumber;
        this.feeType = feeType;
        this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        this.paidAmount = BigDecimal.ZERO.setScale(2);
        this.remainingBalance = this.totalAmount;
        this.dueDate = dueDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        recalculateStatus(LocalDate.now());
    }

    public void recalculateStatus(LocalDate currentDate) {
        if (currentDate == null) {
            currentDate = LocalDate.now();
        }
        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
        if (this.paidAmount == null) {
            this.paidAmount = BigDecimal.ZERO;
        }

        this.totalAmount = this.totalAmount.setScale(2, RoundingMode.HALF_UP);
        this.paidAmount = this.paidAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal balance = this.totalAmount.subtract(this.paidAmount).setScale(2, RoundingMode.HALF_UP);
        if (balance.signum() < 0 || balance.compareTo(new BigDecimal("0.01")) < 0) {
            balance = BigDecimal.ZERO.setScale(2);
        }
        this.remainingBalance = balance;

        if (this.remainingBalance.signum() <= 0) {
            this.status = PaymentStatus.PAID;
            return;
        }

        if (this.dueDate.isBefore(currentDate)) {

            this.status = PaymentStatus.OVERDUE;
        } else if (!this.dueDate.isAfter(currentDate.plusDays(7))) {

            this.status = PaymentStatus.UPCOMING;
        } else if (this.paidAmount.signum() > 0) {

            this.status = PaymentStatus.PARTIALLY_PAID;
        } else {

            this.status = PaymentStatus.PENDING;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public long getDaysOverdue(LocalDate currentDate) {
        if (currentDate == null) currentDate = LocalDate.now();
        if (this.dueDate != null && this.dueDate.isBefore(currentDate)
                && this.remainingBalance != null && this.remainingBalance.signum() > 0) {
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount != null ? BigDecimal.valueOf(totalAmount) : null;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount != null ? BigDecimal.valueOf(paidAmount) : null;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public void setRemainingBalance(Double remainingBalance) {
        this.remainingBalance = remainingBalance != null ? BigDecimal.valueOf(remainingBalance) : null;
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
