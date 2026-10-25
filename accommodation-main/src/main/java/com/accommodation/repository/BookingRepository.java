package com.accommodation.repository;

import com.accommodation.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :bookingId")
    Optional<Booking> findByIdForUpdate(@Param("bookingId") Long bookingId);

    List<Booking> findByStatus(String status);

    List<Booking> findByStudentId(Long studentId);

    List<Booking> findByRoomId(Long roomId);

    boolean existsByStudentIdAndStatus(Long studentId, String status);

    boolean existsByStudentIdAndRoomIdAndStatus(Long studentId, Long roomId, String status);

    @Query("SELECT b FROM Booking b WHERE b.student.id = :studentId AND b.status = 'ACTIVE'")
    List<Booking> findActiveBookingsByStudentHql(@Param("studentId") Long studentId);
}
