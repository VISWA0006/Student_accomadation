package com.accommodation.service;

import com.accommodation.entity.AccommodationStay;
import com.accommodation.entity.Booking;
import com.accommodation.exception.ResourceNotFoundException;
import com.accommodation.repository.AccommodationStayRepository;
import com.accommodation.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccommodationStayService {

    private final AccommodationStayRepository stayRepository;
    private final BookingRepository bookingRepository;

    public AccommodationStayService(AccommodationStayRepository stayRepository,
                                    BookingRepository bookingRepository) {
        this.stayRepository = stayRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public AccommodationStay initialize(Booking booking) {
        if (stayRepository.existsByBookingId(booking.getId())) {
            return stayRepository.findByBookingIdForUpdate(booking.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Accommodation stay not found"));
        }
        return stayRepository.save(new AccommodationStay(booking));
    }

    @Transactional
    public AccommodationStay checkIn(Long bookingId) {
        AccommodationStay stay = getStayForUpdate(bookingId);
        if (!"ACTIVE".equalsIgnoreCase(stay.getBooking().getStatus())) {
            throw new IllegalStateException("Only active bookings can be checked in.");
        }
        if (stay.getCheckOutAt() != null) {
            throw new IllegalStateException("This stay has already been checked out.");
        }
        stay.checkIn();
        return stay;
    }

    @Transactional
    public AccommodationStay checkOut(Long bookingId, String note) {
        AccommodationStay stay = getStayForUpdate(bookingId);
        stay.checkOut(note == null ? null : note.trim());
        return stay;
    }

    private AccommodationStay getStayForUpdate(Long bookingId) {
        return stayRepository.findByBookingIdForUpdate(bookingId)
                .orElseGet(() -> {
                    Booking booking = bookingRepository.findById(bookingId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Booking not found with ID: " + bookingId));
                    return stayRepository.save(new AccommodationStay(booking));
                });
    }
}
