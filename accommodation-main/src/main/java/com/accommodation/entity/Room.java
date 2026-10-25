package com.accommodation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @NotBlank(message = "Room number is required")
    @Column(name = "room_number", nullable = false, unique = true)
    private String roomNumber;

    @NotBlank(message = "Building name is required")
    @Column(name = "building_name", nullable = false)
    private String buildingName;

    @NotBlank(message = "Room type is required")
    @Column(name = "room_type", nullable = false)
    private String roomType;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull(message = "Occupied beds is required")
    @Min(value = 0, message = "Occupied beds cannot be negative")
    @Column(name = "occupied_beds", nullable = false)
    private Integer occupiedBeds = 0;

    @NotNull(message = "Monthly rent is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly rent must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Monthly rent must have at most two decimal places")
    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @NotBlank(message = "Hostel type is required")
    @Column(name = "hostel_type", nullable = false)
    private String hostelType = "BOYS";

    @Column(name = "status", nullable = false)
    private String status = "AVAILABLE";

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    public Room() {
    }

    public Room(String roomNumber, String buildingName, String roomType, Integer capacity, Integer occupiedBeds, Double monthlyRent) {
        this(roomNumber, buildingName, roomType, capacity, occupiedBeds, monthlyRent, "BOYS");
    }

    public Room(String roomNumber, String buildingName, String roomType, Integer capacity, Integer occupiedBeds, Double monthlyRent, String hostelType) {
        this(roomNumber, buildingName, roomType, capacity, occupiedBeds,
                BigDecimal.valueOf(monthlyRent).setScale(2, RoundingMode.HALF_UP), hostelType);
    }

    public Room(String roomNumber, String buildingName, String roomType, Integer capacity, Integer occupiedBeds,
                BigDecimal monthlyRent, String hostelType) {
        this.roomNumber = roomNumber;
        this.buildingName = buildingName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.occupiedBeds = occupiedBeds != null ? occupiedBeds : 0;
        this.monthlyRent = monthlyRent;
        this.hostelType = hostelType != null ? hostelType.trim().toUpperCase() : "BOYS";
        updateAvailability();
    }

    public Integer getAvailableBeds() {
        int cap = capacity != null ? capacity : 0;
        int occ = occupiedBeds != null ? occupiedBeds : 0;
        return Math.max(0, cap - occ);
    }

    public void updateAvailability() {
        if (getAvailableBeds() <= 0) {
            this.status = "FULL";
        } else {
            this.status = "AVAILABLE";
        }
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
        updateAvailability();
    }

    public Integer getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(Integer occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
        updateAvailability();
    }

    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }

    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent != null ? monthlyRent.setScale(2, RoundingMode.HALF_UP) : null;
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

    public String getHostelType() {
        return hostelType;
    }

    public void setHostelType(String hostelType) {
        this.hostelType = hostelType != null ? hostelType.trim().toUpperCase() : "BOYS";
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomNumber='" + roomNumber + '\'' +
                ", buildingName='" + buildingName + '\'' +
                ", roomType='" + roomType + '\'' +
                ", capacity=" + capacity +
                ", occupiedBeds=" + occupiedBeds +
                ", monthlyRent=" + monthlyRent +
                ", hostelType='" + hostelType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
