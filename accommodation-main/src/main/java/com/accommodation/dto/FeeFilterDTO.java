package com.accommodation.dto;

import com.accommodation.entity.PaymentStatus;

public class FeeFilterDTO {

    private String hostelName;
    private String roomNumber;
    private String search;
    private PaymentStatus status;
    private String overdueDays; // "ALL", "7", "15", "30"

    public FeeFilterDTO() {
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = (hostelName != null && !hostelName.trim().isEmpty()) ? hostelName.trim() : null;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = (roomNumber != null && !roomNumber.trim().isEmpty()) ? roomNumber.trim() : null;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(String overdueDays) {
        this.overdueDays = overdueDays;
    }
}
