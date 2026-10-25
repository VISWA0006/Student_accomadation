package com.accommodation.entity;

public enum NotificationCategory {
    UPCOMING_FEE("Upcoming Fee", "⏳"),
    PAYMENT_DUE("Payment Due", "⚠️"),
    OVERDUE_FEE("Overdue Fee", "🚨"),
    PAYMENT_CONFIRMATION("Payment Confirmation", "✅"),
    PARTIAL_PAYMENT("Partial Payment", "💳"),
    ADMINISTRATIVE_ALERT("Administrative Alert", "📢"),
    BOOKING_UPDATE("Booking Update", "🛏️");

    private final String displayName;
    private final String icon;

    NotificationCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }
}
