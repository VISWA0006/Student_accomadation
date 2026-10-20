package com.accommodation.controller.api;

import com.accommodation.dto.BookingRequestDTO;
import com.accommodation.entity.Booking;
import com.accommodation.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        List<Map<String, Object>> response = bookingService.getAllBookings().stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBookingById(@PathVariable("id") Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(toMap(booking));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(@Valid @RequestBody BookingRequestDTO bookingRequest) {
        Booking booking = bookingService.createBooking(bookingRequest);
        return new ResponseEntity<>(toMap(booking), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(@PathVariable("id") Long id) {
        Booking booking = bookingService.cancelBooking(id);
        Map<String, Object> response = toMap(booking);
        response.put("message", "Booking cancelled successfully and room bed released.");
        return ResponseEntity.ok(response);
    }

    /**
     * Demonstrates HQL Query 2 via REST endpoint.
     */
    @GetMapping("/student/{studentId}/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveBookingsForStudent(@PathVariable("studentId") Long studentId) {
        List<Map<String, Object>> response = bookingService.getActiveBookingsByStudentHql(studentId).stream()
                .map(this::toMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> toMap(Booking booking) {
        Map<String, Object> map = new HashMap<>();
        map.put("bookingId", booking.getId());
        map.put("bookingDate", booking.getBookingDate());
        map.put("status", booking.getStatus());

        if (booking.getStudent() != null) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("id", booking.getStudent().getId());
            studentMap.put("fullName", booking.getStudent().getFullName());
            studentMap.put("registerNumber", booking.getStudent().getRegisterNumber());
            studentMap.put("email", booking.getStudent().getEmail());
            studentMap.put("department", booking.getStudent().getDepartment());
            map.put("student", studentMap);
        }

        if (booking.getRoom() != null) {
            Map<String, Object> roomMap = new HashMap<>();
            roomMap.put("id", booking.getRoom().getId());
            roomMap.put("roomNumber", booking.getRoom().getRoomNumber());
            roomMap.put("buildingName", booking.getRoom().getBuildingName());
            roomMap.put("roomType", booking.getRoom().getRoomType());
            roomMap.put("monthlyRent", booking.getRoom().getMonthlyRent());
            map.put("room", roomMap);
        }

        return map;
    }
}
