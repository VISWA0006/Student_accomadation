package com.accommodation.repository;

import com.accommodation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByRoomNumber(String roomNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :roomId")
    Optional<Room> findByIdForUpdate(@Param("roomId") Long roomId);

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByStatus(String status);

    List<Room> findByRoomType(String roomType);

    List<Room> findByHostelType(String hostelType);

    List<Room> findByHostelTypeAndStatus(String hostelType, String status);

    List<Room> findByRoomTypeAndStatus(String roomType, String status);

    long countByStatus(String status);

    long countByHostelType(String hostelType);
}
