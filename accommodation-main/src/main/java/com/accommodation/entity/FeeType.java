package com.accommodation.entity;

public enum FeeType {
    MONTHLY_HOSTEL_FEE("Monthly Hostel Fee"),
    MESS_FEE("Mess Fee"),
    MAINTENANCE_FEE("Maintenance Fee"),
    OTHER_CHARGES("Other Hostel Charges");

    private final String displayName;

    FeeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
