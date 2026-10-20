package com.accommodation.service;

import com.accommodation.dto.FeeFilterDTO;
import com.accommodation.dto.FeeStatsDTO;
import com.accommodation.dto.HostelFeeRequestDTO;
import com.accommodation.dto.NotificationPayload;
import com.accommodation.dto.RecordPaymentDTO;
import com.accommodation.entity.*;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.exception.ResourceNotFoundException;
import com.accommodation.repository.HostelFeeRepository;
import com.accommodation.repository.PaymentTransactionRepository;
import com.accommodation.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HostelFeeService {

    private static final Logger log = LoggerFactory.getLogger(HostelFeeService.class);

    private final HostelFeeRepository feeRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final StudentRepository studentRepository;
    private final InAppNotificationService inAppNotificationService;
    private final NotificationClientService notificationClientService;

    public HostelFeeService(HostelFeeRepository feeRepository,
                            PaymentTransactionRepository transactionRepository,
                            StudentRepository studentRepository,
                            InAppNotificationService inAppNotificationService,
                            NotificationClientService notificationClientService) {
        this.feeRepository = feeRepository;
        this.transactionRepository = transactionRepository;
        this.studentRepository = studentRepository;
        this.inAppNotificationService = inAppNotificationService;
        this.notificationClientService = notificationClientService;
    }

    /**
     * Creates a new hostel fee assignment for a student.
     */
    @Transactional
    public HostelFee createFee(HostelFeeRequestDTO request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        HostelFee fee = new HostelFee(
                student,
                request.getHostelName().trim(),
                request.getRoomNumber().trim(),
                request.getFeeType(),
                request.getTotalAmount(),
                request.getDueDate()
        );

        HostelFee saved = feeRepository.save(fee);

        // Notify student about new fee assignment
        String dedupKey = "FEE_ASSIGNED-" + saved.getId();
        inAppNotificationService.createNotification(
                "STUDENT",
                student,
                saved,
                NotificationCategory.UPCOMING_FEE,
                "New " + saved.getFeeType().getDisplayName() + " Assigned",
                "A fee charge of ₹" + saved.getTotalAmount() + " for " + saved.getHostelName() +
                        " (Room " + saved.getRoomNumber() + ") is due on " + saved.getDueDate() + ".",
                "/student/fees",
                dedupKey
        );

        // Notify external microservice
        notificationClientService.sendNotification(new NotificationPayload(
                student.getFullName(),
                student.getEmail(),
                saved.getRoomNumber(),
                saved.getStatus().name(),
                "FEE_ASSIGNED",
                saved.getFeeType().getDisplayName(),
                saved.getTotalAmount(),
                saved.getRemainingBalance(),
                null,
                "Hostel fee of ₹" + saved.getTotalAmount() + " assigned to " + student.getFullName()
        ));

        log.info("Hostel fee #{} created for student {} (Amount: ₹{}, Due: {})",
                saved.getId(), student.getFullName(), saved.getTotalAmount(), saved.getDueDate());

        return saved;
    }

    public HostelFee getFeeById(Long id) {
        HostelFee fee = feeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel Fee not found with ID: " + id));
        // Dynamically verify and refresh status against today's date
        fee.recalculateStatus(LocalDate.now());
        return fee;
    }

    public List<HostelFee> getAllFees() {
        LocalDate today = LocalDate.now();
        List<HostelFee> fees = feeRepository.findAll();
        fees.forEach(f -> f.recalculateStatus(today));
        return fees;
    }

    public List<HostelFee> getFeesByStudent(Long studentId) {
        LocalDate today = LocalDate.now();
        List<HostelFee> fees = feeRepository.findByStudentIdOrderByDueDateDesc(studentId);
        fees.forEach(f -> f.recalculateStatus(today));
        return fees;
    }

    public List<HostelFee> filterFees(FeeFilterDTO filter) {
        LocalDate today = LocalDate.now();
        List<HostelFee> fees = feeRepository.filterFees(
                filter.getHostelName(),
                filter.getRoomNumber(),
                filter.getSearch(),
                filter.getStatus()
        );

        // Always recompute dynamically in case due dates elapsed
        fees.forEach(f -> f.recalculateStatus(today));

        // Apply overdue days duration filter if specified
        if (filter.getOverdueDays() != null && !filter.getOverdueDays().equalsIgnoreCase("ALL")) {
            try {
                long minDays = Long.parseLong(filter.getOverdueDays());
                fees = fees.stream()
                        .filter(f -> f.getStatus() == PaymentStatus.OVERDUE && f.getDaysOverdue(today) >= minDays)
                        .collect(Collectors.toList());
            } catch (NumberFormatException ignored) {
            }
        }

        return fees;
    }

    public List<HostelFee> getOverdueFees(String durationFilter) {
        LocalDate today = LocalDate.now();
        List<HostelFee> allFees = feeRepository.findAll();
        allFees.forEach(f -> f.recalculateStatus(today));

        long minDays = 0;
        if (durationFilter != null && !durationFilter.equalsIgnoreCase("ALL")) {
            try {
                minDays = Long.parseLong(durationFilter);
            } catch (NumberFormatException ignored) {
            }
        }

        final long threshold = minDays;
        return allFees.stream()
                .filter(f -> f.getStatus() == PaymentStatus.OVERDUE && f.getDaysOverdue(today) >= threshold)
                .sorted((a, b) -> Long.compare(b.getDaysOverdue(today), a.getDaysOverdue(today)))
                .collect(Collectors.toList());
    }

    public FeeStatsDTO getFeeStats() {
        // Refresh statuses across fees to ensure accurate live counts
        LocalDate today = LocalDate.now();
        List<HostelFee> allFees = feeRepository.findAll();
        allFees.forEach(f -> f.recalculateStatus(today));

        Double totalDue = allFees.stream().mapToDouble(HostelFee::getTotalAmount).sum();
        Double totalPaid = allFees.stream().mapToDouble(HostelFee::getPaidAmount).sum();
        Double totalBalance = allFees.stream().mapToDouble(HostelFee::getRemainingBalance).sum();

        long overdueStudentsCount = allFees.stream()
                .filter(f -> f.getStatus() == PaymentStatus.OVERDUE)
                .map(f -> f.getStudent().getId())
                .distinct()
                .count();

        long partiallyPaidStudentsCount = allFees.stream()
                .filter(f -> f.getStatus() == PaymentStatus.PARTIALLY_PAID)
                .map(f -> f.getStudent().getId())
                .distinct()
                .count();

        long upcomingPaymentsCount = allFees.stream()
                .filter(f -> f.getStatus() == PaymentStatus.UPCOMING)
                .count();

        return new FeeStatsDTO(totalDue, totalPaid, totalBalance,
                overdueStudentsCount, partiallyPaidStudentsCount, upcomingPaymentsCount);
    }

    /**
     * Records a payment against a hostel fee record inside an ACID transaction.
     * Enforces all business rules:
     * - Payment amount must be > 0 and cannot exceed remaining balance.
     * - Duplicate transaction references are rejected.
     * - Automatically updates remaining balance and payment status.
     * - Fully paid fees are never marked overdue.
     * - Dispatches in-app notifications and microservice alerts.
     */
    @Transactional
    public PaymentTransaction recordPayment(Long feeId, RecordPaymentDTO paymentDTO) {
        HostelFee fee = feeRepository.findById(feeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel Fee not found with ID: " + feeId));

        LocalDate today = LocalDate.now();
        fee.recalculateStatus(today);

        // Validation: Cannot pay already fully paid fee
        if (fee.getStatus() == PaymentStatus.PAID || fee.getRemainingBalance() <= 0.0) {
            throw new IllegalArgumentException("This hostel fee has already been PAID in full! Remaining balance is ₹0.00.");
        }

        // Validation: Payment amount must be > 0
        if (paymentDTO.getAmount() == null || paymentDTO.getAmount() <= 0.0) {
            throw new IllegalArgumentException("Payment amount must be greater than ₹0.00.");
        }

        // Validation: Payment amount must not exceed remaining balance
        if (paymentDTO.getAmount() > (fee.getRemainingBalance() + 0.001)) {
            throw new IllegalArgumentException("Payment amount (₹" + paymentDTO.getAmount() +
                    ") exceeds outstanding balance (₹" + fee.getRemainingBalance() + ")! Overpayments are not supported.");
        }

        // Validation: Prevent duplicate payment entries
        String txnRef = paymentDTO.getTransactionReference() != null ? paymentDTO.getTransactionReference().trim() : "";
        if (txnRef.isEmpty()) {
            throw new IllegalArgumentException("Transaction reference is required.");
        }
        if (transactionRepository.existsByTransactionReference(txnRef)) {
            throw new DuplicateResourceException("A payment transaction with reference '" + txnRef + "' already exists!");
        }

        // Generate unique official receipt number
        String receiptNumber = "REC-" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" +
                UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Update fee financials
        double newPaidAmount = Math.round((fee.getPaidAmount() + paymentDTO.getAmount()) * 100.0) / 100.0;
        fee.setPaidAmount(newPaidAmount);
        fee.setPaymentDate(paymentDTO.getPaymentDate() != null ? paymentDTO.getPaymentDate() : today);
        fee.setPaymentMethod(paymentDTO.getPaymentMethod());
        fee.setTransactionReference(txnRef);

        // Recompute status (will transition to PAID if balance reaches 0, or stay PARTIALLY_PAID / OVERDUE)
        fee.recalculateStatus(today);
        HostelFee updatedFee = feeRepository.save(fee);

        // Create and save payment transaction audit record
        PaymentTransaction transaction = new PaymentTransaction(
                updatedFee,
                fee.getStudent(),
                paymentDTO.getAmount(),
                paymentDTO.getPaymentDate() != null ? paymentDTO.getPaymentDate() : today,
                paymentDTO.getPaymentMethod(),
                txnRef,
                receiptNumber,
                paymentDTO.getRecordedBy() != null ? paymentDTO.getRecordedBy() : "Hostel Administrator",
                paymentDTO.getNotes()
        );
        PaymentTransaction savedTransaction = transactionRepository.save(transaction);

        // Automated In-App & External Notifications
        boolean isFullyPaid = updatedFee.getStatus() == PaymentStatus.PAID;
        String actionType = isFullyPaid ? "PAYMENT_SUCCESS" : "PARTIAL_PAYMENT";
        NotificationCategory notifCat = isFullyPaid ? NotificationCategory.PAYMENT_CONFIRMATION : NotificationCategory.PARTIAL_PAYMENT;

        String studentTitle = isFullyPaid ? "Payment Received - Fully Paid" : "Partial Payment Recorded";
        String studentMsg = isFullyPaid
                ? "Dear " + fee.getStudent().getFullName() + ", your payment of ₹" + paymentDTO.getAmount() +
                  " for " + fee.getFeeType().getDisplayName() + " has been verified. Remaining balance: ₹0.00. Receipt: " + receiptNumber
                : "Dear " + fee.getStudent().getFullName() + ", your partial payment of ₹" + paymentDTO.getAmount() +
                  " has been recorded. Remaining balance: ₹" + updatedFee.getRemainingBalance() +
                  " due on " + updatedFee.getDueDate() + ". Receipt: " + receiptNumber;

        inAppNotificationService.createNotification(
                "STUDENT",
                fee.getStudent(),
                updatedFee,
                notifCat,
                studentTitle,
                studentMsg,
                "/fees/receipts/" + receiptNumber,
                "PAYMENT-" + savedTransaction.getId()
        );

        // Admin notification
        String adminMsg = "Payment of ₹" + paymentDTO.getAmount() + " recorded for " + fee.getStudent().getFullName() +
                " (" + fee.getFeeType().getDisplayName() + ", Room " + fee.getRoomNumber() + "). " +
                (isFullyPaid ? "Fee is now fully settled." : "Remaining balance: ₹" + updatedFee.getRemainingBalance());

        inAppNotificationService.createNotification(
                "ADMIN",
                null,
                updatedFee,
                NotificationCategory.ADMINISTRATIVE_ALERT,
                "Payment Recorded: " + fee.getStudent().getFullName(),
                adminMsg,
                "/fees/" + updatedFee.getId(),
                "ADMIN_PAYMENT-" + savedTransaction.getId()
        );

        // External notification service event
        notificationClientService.sendNotification(new NotificationPayload(
                fee.getStudent().getFullName(),
                fee.getStudent().getEmail(),
                fee.getRoomNumber(),
                updatedFee.getStatus().name(),
                actionType,
                fee.getFeeType().getDisplayName(),
                paymentDTO.getAmount(),
                updatedFee.getRemainingBalance(),
                receiptNumber,
                studentMsg
        ));

        log.info("Payment of ₹{} processed for fee #{} (Receipt: {}, Status: {})",
                paymentDTO.getAmount(), fee.getId(), receiptNumber, updatedFee.getStatus());

        return savedTransaction;
    }

    public List<PaymentTransaction> getTransactionsForFee(Long feeId) {
        return transactionRepository.findByHostelFeeIdOrderByPaymentDateDescCreatedAtDesc(feeId);
    }

    public List<PaymentTransaction> getTransactionsForStudent(Long studentId) {
        return transactionRepository.findByStudentIdOrderByPaymentDateDescCreatedAtDesc(studentId);
    }

    public PaymentTransaction getTransactionByReceipt(String receiptNumber) {
        return transactionRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found: " + receiptNumber));
    }
}
