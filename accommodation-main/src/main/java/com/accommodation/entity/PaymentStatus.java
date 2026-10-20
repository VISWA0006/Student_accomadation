package com.accommodation.entity;

public enum PaymentStatus {
    PAID("Paid", "badge-paid"),
    PARTIALLY_PAID("Partially Paid", "badge-partially-paid"),
    PENDING("Pending", "badge-pending"),
    UPCOMING("Upcoming", "badge-upcoming"),
    OVERDUE("Overdue", "badge-overdue");

    private final String displayName;
    private final String badgeClass;

    PaymentStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
