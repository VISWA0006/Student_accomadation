package com.accommodation.repository;

import com.accommodation.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatus(String status);

    List<Booking> findByStudentId(Long studentId);

    List<Booking> findByRoomId(Long roomId);

    boolean existsByStudentIdAndStatus(Long studentId, String status);

    /**
     * Finds all ACTIVE bookings for a given student via HQL.
     * References the entity name 'Booking' and navigated association 'b.student.id'.
     */
    @Query("SELECT b FROM Booking b WHERE b.student.id = :studentId AND b.status = 'ACTIVE'")
    List<Booking> findActiveBookingsByStudentHql(@Param("studentId") Long studentId);
}
