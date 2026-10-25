package com.accommodation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true)
    private Student student;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "password_change_required", nullable = false)
    private boolean passwordChangeRequired;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected UserAccount() {
    }

    public UserAccount(String username, String passwordHash, String role, Student student) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.student = student;
        this.enabled = true;
        this.passwordChangeRequired = "STUDENT".equals(role);
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public Student getStudent() {
        return student;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPasswordChangeRequired() {
        return passwordChangeRequired;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = passwordHash;
        this.passwordChangeRequired = false;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
