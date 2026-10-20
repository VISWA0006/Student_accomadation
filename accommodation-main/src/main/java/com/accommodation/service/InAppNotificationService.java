package com.accommodation.service;

import com.accommodation.entity.HostelFee;
import com.accommodation.entity.InAppNotification;
import com.accommodation.entity.NotificationCategory;
import com.accommodation.entity.Student;
import com.accommodation.repository.InAppNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationService.class);

    private final InAppNotificationRepository notificationRepository;

    public InAppNotificationService(InAppNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Creates an in-app notification with deduplication.
     * If a notification with the same dedupKey already exists, skips creation.
     */
    @Transactional
    public Optional<InAppNotification> createNotification(String recipientRole, Student student,
                                                          HostelFee hostelFee, NotificationCategory category,
                                                          String title, String message, String linkUrl,
                                                          String dedupKey) {
        if (dedupKey != null && notificationRepository.existsByDedupKey(dedupKey)) {
            log.debug("Skipping duplicate notification with dedupKey: {}", dedupKey);
            return Optional.empty();
        }

        InAppNotification notification = new InAppNotification(
                recipientRole, student, hostelFee, category, title, message, linkUrl, dedupKey
        );
        InAppNotification saved = notificationRepository.save(notification);
        log.info("In-App Notification created: id={}, role={}, category={}, title='{}'",
                saved.getId(), recipientRole, category, title);
        return Optional.of(saved);
    }

    public List<InAppNotification> getNotificationsForStudent(Long studentId, NotificationCategory category) {
        if (category != null) {
            return notificationRepository.findByStudentIdAndCategoryOrderByCreatedAtDesc(studentId, category);
        }
        return notificationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    public List<InAppNotification> getNotificationsForAdmin(NotificationCategory category) {
        if (category != null) {
            return notificationRepository.findByRecipientRoleAndCategoryOrderByCreatedAtDesc("ADMIN", category);
        }
        return notificationRepository.findByRecipientRoleOrderByCreatedAtDesc("ADMIN");
    }

    public long getUnreadCountForStudent(Long studentId) {
        return notificationRepository.countByStudentIdAndIsReadFalse(studentId);
    }

    public long getUnreadCountForAdmin() {
        return notificationRepository.countByRecipientRoleAndIsReadFalse("ADMIN");
    }

    @Transactional
    public boolean markAsRead(Long notificationId) {
        return notificationRepository.findById(notificationId).map(n -> {
            n.setRead(true);
            notificationRepository.save(n);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void markAllAsReadForStudent(Long studentId) {
        notificationRepository.markAllAsReadForStudent(studentId);
    }

    @Transactional
    public void markAllAsReadForAdmin() {
        notificationRepository.markAllAsReadForAdmin();
    }
}
