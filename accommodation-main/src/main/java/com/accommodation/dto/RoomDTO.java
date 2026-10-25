package com.accommodation.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoomDTO {

    private Long id;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Building name is required")
    private String buildingName;

    @NotBlank(message = "Room type is required")
    private String roomType;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private Integer occupiedBeds = 0;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly rent must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Monthly rent must have at most two decimal places")
    private BigDecimal monthlyRent;

    @NotBlank(message = "Hostel type is required")
    private String hostelType = "BOYS";

    private String status = "AVAILABLE";
    private Integer availableBeds;

    public RoomDTO() {
    }

    public RoomDTO(Long id, String roomNumber, String buildingName, String roomType, Integer capacity, Integer occupiedBeds, Double monthlyRent, String status, Integer availableBeds) {
        this(id, roomNumber, buildingName, roomType, capacity, occupiedBeds, monthlyRent, "BOYS", status, availableBeds);
    }

    public RoomDTO(Long id, String roomNumber, String buildingName, String roomType, Integer capacity, Integer occupiedBeds, Double monthlyRent, String hostelType, String status, Integer availableBeds) {
        this(id, roomNumber, buildingName, roomType, capacity, occupiedBeds,
                monthlyRent != null ? BigDecimal.valueOf(monthlyRent).setScale(2, RoundingMode.HALF_UP) : null,
                hostelType, status, availableBeds);
    }

    public RoomDTO(Long id, String roomNumber, String buildingName, String roomType, Integer capacity,
                   Integer occupiedBeds, BigDecimal monthlyRent, String hostelType, String status,
                   Integer availableBeds) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.buildingName = buildingName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.occupiedBeds = occupiedBeds;
        this.monthlyRent = monthlyRent;
        this.hostelType = hostelType != null ? hostelType.trim().toUpperCase() : "BOYS";
        this.status = status;
        this.availableBeds = availableBeds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(Integer occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }

    public void setMonthlyRent(Double monthlyRent) {
        this.monthlyRent = monthlyRent != null
                ? BigDecimal.valueOf(monthlyRent).setScale(2, RoundingMode.HALF_UP) : null;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(Integer availableBeds) {
        this.availableBeds = availableBeds;
    }

    public String getHostelType() {
        return hostelType;
    }

    public void setHostelType(String hostelType) {
        this.hostelType = hostelType != null ? hostelType.trim().toUpperCase() : "BOYS";
    }
}
