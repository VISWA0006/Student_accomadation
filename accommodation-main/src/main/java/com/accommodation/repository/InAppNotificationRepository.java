package com.accommodation.repository;

import com.accommodation.entity.InAppNotification;
import com.accommodation.entity.NotificationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InAppNotificationRepository extends JpaRepository<InAppNotification, Long> {

    List<InAppNotification> findByStudentIdAndIsReadFalseOrderByCreatedAtDesc(Long studentId);

    List<InAppNotification> findByRecipientRoleAndIsReadFalseOrderByCreatedAtDesc(String recipientRole);

    List<InAppNotification> findByStudentIdAndCategoryAndIsReadFalseOrderByCreatedAtDesc(
            Long studentId, NotificationCategory category);

    List<InAppNotification> findByRecipientRoleAndCategoryAndIsReadFalseOrderByCreatedAtDesc(
            String recipientRole, NotificationCategory category);

    long countByStudentIdAndIsReadFalse(Long studentId);

    long countByRecipientRoleAndIsReadFalse(String recipientRole);

    boolean existsByDedupKey(String dedupKey);

    java.util.Optional<InAppNotification> findByIdAndStudentId(Long id, Long studentId);

    java.util.Optional<InAppNotification> findByIdAndRecipientRole(Long id, String recipientRole);

    @Modifying
    @Query("UPDATE InAppNotification n SET n.isRead = true WHERE n.student.id = :studentId")
    void markAllAsReadForStudent(@Param("studentId") Long studentId);

    @Modifying
    @Query("UPDATE InAppNotification n SET n.isRead = true WHERE n.recipientRole = 'ADMIN'")
    void markAllAsReadForAdmin();
}
