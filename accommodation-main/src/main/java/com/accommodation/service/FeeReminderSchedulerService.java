package com.accommodation.service;

import com.accommodation.entity.*;
import com.accommodation.repository.HostelFeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Automated scheduled job service that scans all hostel fee records,
 * recalculates their live status, and dispatches upcoming, due today, and recurring overdue alerts.
 * Uses deterministic deduplication keys so repeated scheduler executions are completely safe and idempotent.
 */
@Service
public class FeeReminderSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(FeeReminderSchedulerService.class);

    private final HostelFeeRepository feeRepository;
    private final InAppNotificationService inAppNotificationService;

    public FeeReminderSchedulerService(HostelFeeRepository feeRepository,
                                       InAppNotificationService inAppNotificationService) {
        this.feeRepository = feeRepository;
        this.inAppNotificationService = inAppNotificationService;
    }

    /**
     * Runs daily at midnight, or manually via trigger.
     * Checks all hostel fee records and triggers appropriate alerts.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public int runFeeAuditAndReminders() {
        log.info("Starting automated Hostel Fee reminder and overdue audit scan...");
        LocalDate today = LocalDate.now();
        List<HostelFee> fees = feeRepository.findAll();
        int notificationsDispatched = 0;

        for (HostelFee fee : fees) {
            // Recompute dynamic status against current date
            fee.recalculateStatus(today);
            feeRepository.save(fee);

            // Fully paid fees never generate reminders
            if (fee.getStatus() == PaymentStatus.PAID || fee.getRemainingBalance().signum() <= 0) {
                continue;
            }

            long daysUntilDue = ChronoUnit.DAYS.between(today, fee.getDueDate());
            long daysOverdue = ChronoUnit.DAYS.between(fee.getDueDate(), today);

            // A. UPCOMING REMINDERS: 7 days, 3 days, 1 day
            if (daysUntilDue == 7) {
                notificationsDispatched += sendUpcomingReminder(fee, 7, today);
            } else if (daysUntilDue == 3) {
                notificationsDispatched += sendUpcomingReminder(fee, 3, today);
            } else if (daysUntilDue == 1) {
                notificationsDispatched += sendUpcomingReminder(fee, 1, today);
            }

            // B. DUE TODAY
            if (daysUntilDue == 0) {
                notificationsDispatched += sendDueTodayReminder(fee, today);
            }

            // C. OVERDUE REMINDERS: Dispatched when overdue, and repeated every 3 days (e.g. Day 1, 4, 7, 10, ...)
            if (fee.getStatus() == PaymentStatus.OVERDUE && daysOverdue > 0) {
                // Trigger on day 1, or every 3 days thereafter
                if (daysOverdue == 1 || (daysOverdue % 3 == 1)) {
                    notificationsDispatched += sendOverdueReminder(fee, daysOverdue, today);
                }
            }
        }

        log.info("Automated fee audit complete. {} new notification alerts processed.", notificationsDispatched);
        return notificationsDispatched;
    }

    private int sendUpcomingReminder(HostelFee fee, int daysLeft, LocalDate today) {
        String dedupKey = "REMIND-" + daysLeft + "D-" + fee.getId() + "-" + fee.getDueDate();
        String title = "Upcoming Fee Reminder: Due in " + daysLeft + " Day" + (daysLeft > 1 ? "s" : "");
        String message = "Dear " + fee.getStudent().getFullName() + ", your " + fee.getFeeType().getDisplayName() +
                " of ₹" + fee.getRemainingBalance() + " for Room " + fee.getRoomNumber() + " is due in " + daysLeft +
                " day(s) on " + fee.getDueDate() + ". Please settle before the due date.";

        var notif = inAppNotificationService.createNotification(
                "STUDENT",
                fee.getStudent(),
                fee,
                NotificationCategory.UPCOMING_FEE,
                title,
                message,
                "/student/fees",
                dedupKey
        );

        return notif.isPresent() ? 1 : 0;
    }

    private int sendDueTodayReminder(HostelFee fee, LocalDate today) {
        String dedupKey = "REMIND-DUETODAY-" + fee.getId() + "-" + today;
        String title = "Payment Due Today!";
        String message = "Urgent: Dear " + fee.getStudent().getFullName() + ", your " + fee.getFeeType().getDisplayName() +
                " of ₹" + fee.getRemainingBalance() + " for Room " + fee.getRoomNumber() + " is due TODAY (" +
                fee.getDueDate() + "). Please make payment to avoid overdue charges.";

        var notif = inAppNotificationService.createNotification(
                "STUDENT",
                fee.getStudent(),
                fee,
                NotificationCategory.PAYMENT_DUE,
                title,
                message,
                "/student/fees",
                dedupKey
        );

        return notif.isPresent() ? 1 : 0;
    }

    private int sendOverdueReminder(HostelFee fee, long daysOverdue, LocalDate today) {
        String dedupKey = "REMIND-OVERDUE-" + fee.getId() + "-DAY-" + daysOverdue;
        String title = "OVERDUE FEE ALERT: " + daysOverdue + " Day" + (daysOverdue > 1 ? "s" : "") + " Overdue";
        String message = "URGENT OVERDUE NOTICE: Dear " + fee.getStudent().getFullName() + ", your " +
                fee.getFeeType().getDisplayName() + " of ₹" + fee.getRemainingBalance() + " (Room " + fee.getRoomNumber() +
                ") was due on " + fee.getDueDate() + " and is now " + daysOverdue + " days overdue! Please clear your balance immediately.";

        var notif = inAppNotificationService.createNotification(
                "STUDENT",
                fee.getStudent(),
                fee,
                NotificationCategory.OVERDUE_FEE,
                title,
                message,
                "/student/fees",
                dedupKey
        );

        // Also alert Administrator
        String adminDedupKey = "ADMIN-OVERDUE-" + fee.getId() + "-DAY-" + daysOverdue;
        inAppNotificationService.createNotification(
                "ADMIN",
                null,
                fee,
                NotificationCategory.ADMINISTRATIVE_ALERT,
                "Overdue Alert: " + fee.getStudent().getFullName() + " (" + daysOverdue + "d overdue)",
                "Student " + fee.getStudent().getFullName() + " has an outstanding overdue balance of ₹" +
                        fee.getRemainingBalance() + " (" + fee.getFeeType().getDisplayName() + ", " + fee.getHostelName() +
                        ", Room " + fee.getRoomNumber() + ").",
                "/fees/" + fee.getId(),
                adminDedupKey
        );

        return notif.isPresent() ? 1 : 0;
    }
}
