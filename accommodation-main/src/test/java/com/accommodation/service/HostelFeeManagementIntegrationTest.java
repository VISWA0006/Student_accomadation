package com.accommodation.service;

import com.accommodation.dto.HostelFeeRequestDTO;
import com.accommodation.dto.RecordPaymentDTO;
import com.accommodation.dto.FeeFilterDTO;
import com.accommodation.dto.FeeStatsDTO;
import com.accommodation.entity.*;
import com.accommodation.repository.HostelFeeRepository;
import com.accommodation.repository.PaymentTransactionRepository;
import com.accommodation.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"h2", "test"})
@Transactional
class HostelFeeManagementIntegrationTest {

    @Autowired
    private HostelFeeService feeService;

    @Autowired
    private HostelFeeRepository feeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    private InAppNotificationService notificationService;

    @Autowired
    private com.accommodation.repository.InAppNotificationRepository notificationRepository;

    @Autowired
    private FeeReminderSchedulerService schedulerService;

    private Student student1;
    private Student student2;

    @BeforeEach
    void setUp() {
        // Clean test slate in proper foreign key order
        notificationRepository.deleteAll();
        transactionRepository.deleteAll();
        feeRepository.deleteAll();
        studentRepository.deleteAll();

        student1 = studentRepository.save(new Student("Test Student One", "REG-001", "st1@test.edu", "9999999991", "CS", 1, "MALE"));
        student2 = studentRepository.save(new Student("Test Student Two", "REG-002", "st2@test.edu", "9999999992", "EC", 2, "FEMALE"));
    }

    @Test
    @DisplayName("Scenario 1: Student pays the entire fee before the due date -> transitions to PAID with zero balance")
    void test1_studentPaysEntireFeeBeforeDueDate() {
        HostelFeeRequestDTO request = new HostelFeeRequestDTO();
        request.setStudentId(student1.getId());
        request.setHostelName("Kaveri Boys Hostel");
        request.setRoomNumber("A-101");
        request.setFeeType(FeeType.MONTHLY_HOSTEL_FEE);
        request.setTotalAmount(new BigDecimal("5000.00"));
        request.setDueDate(LocalDate.now().plusDays(10));

        HostelFee fee = feeService.createFee(request);
        assertEquals(new BigDecimal("5000.00"), fee.getRemainingBalance());

        RecordPaymentDTO payment = new RecordPaymentDTO();
        payment.setAmount(new BigDecimal("5000.00"));
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod("UPI");
        payment.setTransactionReference("TXN-TEST-FULL-01");

        PaymentTransaction tx = feeService.recordPayment(fee.getId(), payment);
        assertNotNull(tx);
        assertNotNull(tx.getReceiptNumber());

        HostelFee updated = feeService.getFeeById(fee.getId());
        assertEquals(PaymentStatus.PAID, updated.getStatus());
        assertEquals(new BigDecimal("0.00"), updated.getRemainingBalance());
        assertEquals(new BigDecimal("5000.00"), updated.getPaidAmount());
    }

    @Test
    @DisplayName("Fee records are paged in the database and dashboard totals use aggregate queries")
    void feeListUsesPaginationAndAggregates() {
        HostelFeeRequestDTO first = new HostelFeeRequestDTO();
        first.setStudentId(student1.getId());
        first.setHostelName("Kaveri Boys Hostel");
        first.setRoomNumber("A-101");
        first.setTotalAmount(new BigDecimal("5000.00"));
        first.setDueDate(LocalDate.now().plusDays(20));
        feeService.createFee(first);

        HostelFeeRequestDTO second = new HostelFeeRequestDTO();
        second.setStudentId(student2.getId());
        second.setHostelName("Yamuna Girls Hostel");
        second.setRoomNumber("G-101");
        second.setTotalAmount(new BigDecimal("6000.00"));
        second.setDueDate(LocalDate.now().plusDays(25));
        feeService.createFee(second);

        Page<HostelFee> page = feeService.getFeePage(new FeeFilterDTO(), PageRequest.of(0, 1));
        FeeStatsDTO stats = feeService.getFeeStats();

        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertEquals(11000.0, stats.getTotalFeesDue());
        assertEquals(11000.0, stats.getTotalOutstandingBalance());
    }

    @Test
    @DisplayName("Scenario 2: Student pays only part of the fee -> balance calculated and status is PARTIALLY_PAID or UPCOMING")
    void test2_studentPaysPartialFee() {
        HostelFeeRequestDTO request = new HostelFeeRequestDTO();
        request.setStudentId(student1.getId());
        request.setHostelName("Kaveri Boys Hostel");
        request.setRoomNumber("A-101");
        request.setFeeType(FeeType.MESS_FEE);
        request.setTotalAmount(new BigDecimal("6000.00"));
        request.setDueDate(LocalDate.now().plusDays(15));

        HostelFee fee = feeService.createFee(request);

        RecordPaymentDTO payment = new RecordPaymentDTO();
        payment.setAmount(new BigDecimal("2500.00"));
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod("Net Banking");
        payment.setTransactionReference("TXN-TEST-PARTIAL-01");

        feeService.recordPayment(fee.getId(), payment);

        HostelFee updated = feeService.getFeeById(fee.getId());
        assertEquals(new BigDecimal("3500.00"), updated.getRemainingBalance());
        assertEquals(new BigDecimal("2500.00"), updated.getPaidAmount());
        assertEquals(PaymentStatus.PARTIALLY_PAID, updated.getStatus());
    }

    @Test
    @DisplayName("Scenario 3: A fee reaches its due date without payment -> marked OVERDUE")
    void test3_feeReachesDueDateWithoutPayment() {
        HostelFee fee = new HostelFee(student1, "Kaveri Boys Hostel", "A-101",
                FeeType.MONTHLY_HOSTEL_FEE, 4000.0, LocalDate.now().minusDays(3));
        fee = feeRepository.save(fee);

        fee.recalculateStatus(LocalDate.now());
        assertEquals(PaymentStatus.OVERDUE, fee.getStatus());
        assertEquals(3, fee.getDaysOverdue(LocalDate.now()));
    }

    @Test
    @DisplayName("Scenario 4: A partially paid fee becomes overdue when due date passes")
    void test4_partiallyPaidFeeBecomesOverdue() {
        HostelFee fee = new HostelFee(student1, "Kaveri Boys Hostel", "A-101",
                FeeType.MAINTENANCE_FEE, 3000.0, LocalDate.now().minusDays(5));
        fee.setPaidAmount(1000.0);
        fee.recalculateStatus(LocalDate.now());
        fee = feeRepository.save(fee);

        assertEquals(new BigDecimal("2000.00"), fee.getRemainingBalance());
        assertEquals(PaymentStatus.OVERDUE, fee.getStatus());
        assertEquals(5, fee.getDaysOverdue(LocalDate.now()));
    }

    @Test
    @DisplayName("Scenario 5: A fully paid fee must NEVER be marked overdue even after due date passes")
    void test5_fullyPaidFeeNeverMarkedOverdue() {
        HostelFee fee = new HostelFee(student1, "Kaveri Boys Hostel", "A-101",
                FeeType.MONTHLY_HOSTEL_FEE, 4000.0, LocalDate.now().minusDays(20));
        fee.setPaidAmount(4000.0);
        fee.recalculateStatus(LocalDate.now());
        fee = feeRepository.save(fee);

        assertEquals(new BigDecimal("0.00"), fee.getRemainingBalance());
        assertEquals(PaymentStatus.PAID, fee.getStatus());
        assertEquals(0, fee.getDaysOverdue(LocalDate.now()));
    }

    @Test
    @DisplayName("Scenario 6: The same reminder is not created repeatedly (Idempotent Deduplication)")
    void test6_sameReminderNotCreatedRepeatedly() {
        String dedupKey = "TEST-DEDUP-KEY-100";

        Optional<InAppNotification> first = notificationService.createNotification(
                "STUDENT", student1, null, NotificationCategory.UPCOMING_FEE,
                "Reminder 1", "Due in 3 days", "/student/fees", dedupKey
        );
        assertTrue(first.isPresent());

        // Attempting to send again with same dedupKey
        Optional<InAppNotification> second = notificationService.createNotification(
                "STUDENT", student1, null, NotificationCategory.UPCOMING_FEE,
                "Reminder 2", "Duplicate message", "/student/fees", dedupKey
        );
        assertTrue(second.isEmpty(), "Duplicate notification must be suppressed by dedupKey");
    }

    @Test
    @DisplayName("Scenario 7: A student can only access their own fee records")
    void test7_studentCannotViewAnotherStudentsFees() {
        HostelFee fee1 = new HostelFee(student1, "Kaveri", "A-101", FeeType.MONTHLY_HOSTEL_FEE, 5000.0, LocalDate.now().plusDays(10));
        HostelFee fee2 = new HostelFee(student2, "Yamuna", "G-101", FeeType.MONTHLY_HOSTEL_FEE, 5000.0, LocalDate.now().plusDays(10));
        feeRepository.saveAll(List.of(fee1, fee2));

        List<HostelFee> student1Fees = feeService.getFeesByStudent(student1.getId());
        assertEquals(1, student1Fees.size());
        assertEquals(student1.getId(), student1Fees.get(0).getStudent().getId());

        List<HostelFee> student2Fees = feeService.getFeesByStudent(student2.getId());
        assertEquals(1, student2Fees.size());
        assertEquals(student2.getId(), student2Fees.get(0).getStudent().getId());
    }

    @Test
    @DisplayName("Scenario 8: Administrator can filter and view overdue records")
    void test8_adminCanFilterAndViewOverdueRecords() {
        HostelFee ov1 = new HostelFee(student1, "Kaveri Boys Hostel", "A-101", FeeType.MONTHLY_HOSTEL_FEE, 5000.0, LocalDate.now().minusDays(10));
        HostelFee ov2 = new HostelFee(student2, "Yamuna Girls Hostel", "G-101", FeeType.MESS_FEE, 3000.0, LocalDate.now().minusDays(20));
        HostelFee normal = new HostelFee(student1, "Kaveri Boys Hostel", "A-101", FeeType.MAINTENANCE_FEE, 1000.0, LocalDate.now().plusDays(15));
        feeRepository.saveAll(List.of(ov1, ov2, normal));

        List<HostelFee> allOverdue = feeService.getOverdueFees("ALL");
        assertEquals(2, allOverdue.size());

        List<HostelFee> over15Days = feeService.getOverdueFees("15");
        assertEquals(1, over15Days.size());
        assertEquals(student2.getId(), over15Days.get(0).getStudent().getId());
    }

    @Test
    @DisplayName("Scenario 9: A fully paid fee no longer generates overdue reminders")
    void test9_fullyPaidFeeNoLongerGeneratesOverdueReminders() {
        HostelFee fee = new HostelFee(student1, "Kaveri", "A-101", FeeType.MONTHLY_HOSTEL_FEE, 5000.0, LocalDate.now().minusDays(10));
        fee.setPaidAmount(5000.0);
        fee.recalculateStatus(LocalDate.now());
        feeRepository.save(fee);

        int alerts = schedulerService.runFeeAuditAndReminders();
        assertEquals(0, alerts, "Zero reminders must be dispatched for fully settled fees");
    }

    @Test
    @DisplayName("Read notifications are excluded from student and admin inboxes")
    void readNotificationsAreExcludedFromInboxes() {
        InAppNotification unreadStudentNotification = notificationService.createNotification(
                "STUDENT", student1, null, NotificationCategory.UPCOMING_FEE,
                "Unread reminder", "Fee is due soon", "/student/fees", "TEST-UNREAD-STUDENT"
        ).orElseThrow();
        InAppNotification readStudentNotification = notificationService.createNotification(
                "STUDENT", student1, null, NotificationCategory.UPCOMING_FEE,
                "Read reminder", "Fee was due soon", "/student/fees", "TEST-READ-STUDENT"
        ).orElseThrow();
        InAppNotification readAdminNotification = notificationService.createNotification(
                "ADMIN", null, null, NotificationCategory.ADMINISTRATIVE_ALERT,
                "Admin alert", "Action required", "/notifications", "TEST-READ-ADMIN"
        ).orElseThrow();

        assertTrue(notificationService.markAsRead(unreadStudentNotification.getId()));
        assertTrue(notificationService.markAsRead(readStudentNotification.getId()));
        assertTrue(notificationService.markAsRead(readAdminNotification.getId()));
        assertTrue(notificationService.getNotificationsForStudent(student1.getId(), null).isEmpty());
        assertTrue(notificationService.getNotificationsForStudent(
                student1.getId(), NotificationCategory.UPCOMING_FEE).isEmpty());
        assertTrue(notificationService.getNotificationsForAdmin(null).isEmpty());
        assertTrue(notificationService.getNotificationsForAdmin(
                NotificationCategory.ADMINISTRATIVE_ALERT).isEmpty());
    }

    @Test
    @DisplayName("Scenario 10: Payment history and balances remain consistent after multiple payments")
    void test10_paymentHistoryAndBalancesRemainConsistent() {
        HostelFeeRequestDTO request = new HostelFeeRequestDTO();
        request.setStudentId(student1.getId());
        request.setHostelName("Kaveri Boys Hostel");
        request.setRoomNumber("A-101");
        request.setFeeType(FeeType.MONTHLY_HOSTEL_FEE);
        request.setTotalAmount(new BigDecimal("10000.00"));
        request.setDueDate(LocalDate.now().plusDays(20));
        HostelFee fee = feeService.createFee(request);

        // Payment 1: ₹3,000
        RecordPaymentDTO p1 = new RecordPaymentDTO();
        p1.setAmount(new BigDecimal("3000.00"));
        p1.setPaymentMethod("UPI");
        p1.setTransactionReference("TXN-MULTI-01");
        feeService.recordPayment(fee.getId(), p1);

        HostelFee state1 = feeService.getFeeById(fee.getId());
        assertEquals(new BigDecimal("3000.00"), state1.getPaidAmount());
        assertEquals(new BigDecimal("7000.00"), state1.getRemainingBalance());
        assertEquals(PaymentStatus.PARTIALLY_PAID, state1.getStatus());

        // Payment 2: ₹4,000
        RecordPaymentDTO p2 = new RecordPaymentDTO();
        p2.setAmount(new BigDecimal("4000.00"));
        p2.setPaymentMethod("Net Banking");
        p2.setTransactionReference("TXN-MULTI-02");
        feeService.recordPayment(fee.getId(), p2);

        HostelFee state2 = feeService.getFeeById(fee.getId());
        assertEquals(new BigDecimal("7000.00"), state2.getPaidAmount());
        assertEquals(new BigDecimal("3000.00"), state2.getRemainingBalance());

        // Payment 3: ₹3,000 (Clears balance)
        RecordPaymentDTO p3 = new RecordPaymentDTO();
        p3.setAmount(new BigDecimal("3000.00"));
        p3.setPaymentMethod("Cash");
        p3.setTransactionReference("TXN-MULTI-03");
        feeService.recordPayment(fee.getId(), p3);

        HostelFee state3 = feeService.getFeeById(fee.getId());
        assertEquals(new BigDecimal("10000.00"), state3.getPaidAmount());
        assertEquals(new BigDecimal("0.00"), state3.getRemainingBalance());
        assertEquals(PaymentStatus.PAID, state3.getStatus());

        // Verify transaction history audit log
        List<PaymentTransaction> txList = feeService.getTransactionsForFee(fee.getId());
        assertEquals(3, txList.size());

        // Attempting an overpayment must fail
        RecordPaymentDTO p4 = new RecordPaymentDTO();
        p4.setAmount(new BigDecimal("500.00"));
        p4.setPaymentMethod("Cash");
        p4.setTransactionReference("TXN-MULTI-04");
        assertThrows(IllegalArgumentException.class, () -> feeService.recordPayment(fee.getId(), p4));
    }
}
