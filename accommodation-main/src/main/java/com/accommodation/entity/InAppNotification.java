package com.accommodation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entity representing an In-App Notification.
 * Mapped to 'in_app_notifications' table.
 * Supports read/unread states, deduplication keys, and role-based filtering.
 */
@Entity
@Table(name = "in_app_notifications",
       indexes = {
           @Index(name = "idx_notif_student", columnList = "student_id"),
           @Index(name = "idx_notif_role", columnList = "recipient_role"),
           @Index(name = "idx_notif_dedup", columnList = "dedup_key", unique = true)
       })
public class InAppNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "recipient_role", nullable = false)
    private String recipientRole; // "STUDENT" or "ADMIN"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_id")
    private HostelFee hostelFee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private NotificationCategory category;

    @NotBlank
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank
    @Column(name = "message", length = 1000, nullable = false)
    private String message;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "dedup_key", unique = true)
    private String dedupKey;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public InAppNotification() {
    }

    public InAppNotification(String recipientRole, Student student, HostelFee hostelFee,
                             NotificationCategory category, String title, String message,
                             String linkUrl, String dedupKey) {
        this.recipientRole = recipientRole;
        this.student = student;
        this.hostelFee = hostelFee;
        this.category = category;
        this.title = title;
        this.message = message;
        this.linkUrl = linkUrl;
        this.dedupKey = dedupKey;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRecipientRole() {
        return recipientRole;
    }

    public void setRecipientRole(String recipientRole) {
        this.recipientRole = recipientRole;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public HostelFee getHostelFee() {
        return hostelFee;
    }

    public void setHostelFee(HostelFee hostelFee) {
        this.hostelFee = hostelFee;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public void setCategory(NotificationCategory category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public void setDedupKey(String dedupKey) {
        this.dedupKey = dedupKey;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
