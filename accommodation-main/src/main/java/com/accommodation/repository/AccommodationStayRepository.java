package com.accommodation.repository;

import com.accommodation.entity.AccommodationStay;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccommodationStayRepository extends JpaRepository<AccommodationStay, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AccommodationStay s WHERE s.booking.id = :bookingId")
    Optional<AccommodationStay> findByBookingIdForUpdate(@Param("bookingId") Long bookingId);

    boolean existsByBookingId(Long bookingId);
}
