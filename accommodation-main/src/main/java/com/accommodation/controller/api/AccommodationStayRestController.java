package com.accommodation.controller.api;

import com.accommodation.entity.AccommodationStay;
import com.accommodation.service.AccommodationStayService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/bookings/{bookingId}/stay")
public class AccommodationStayRestController {

    private final AccommodationStayService stayService;

    public AccommodationStayRestController(AccommodationStayService stayService) {
        this.stayService = stayService;
    }

    @PostMapping("/check-in")
    @Transactional
    public StayView checkIn(@PathVariable Long bookingId) {
        return toView(stayService.checkIn(bookingId));
    }

    @PostMapping("/check-out")
    @Transactional
    public StayView checkOut(@PathVariable Long bookingId,
                             @Valid @RequestBody(required = false) CheckOutRequest request) {
        return toView(stayService.checkOut(bookingId, request == null ? null : request.note()));
    }

    private StayView toView(AccommodationStay stay) {
        return new StayView(stay.getId(), stay.getBooking().getId(), stay.getBooking().getStatus(),
                stay.getCheckInAt(), stay.getCheckOutAt(), stay.getCheckOutNote());
    }

    public record CheckOutRequest(@Size(max = 1000) String note) {
    }

    public record StayView(Long id, Long bookingId, String bookingStatus,
                           LocalDateTime checkInAt, LocalDateTime checkOutAt, String checkOutNote) {
    }

}
