package com.accommodation.service;

import com.accommodation.entity.Room;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.exception.ResourceNotFoundException;
import com.accommodation.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + id));
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByStatus("AVAILABLE");
    }

    @Transactional
    public Room addRoom(Room room) {
        if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new DuplicateResourceException("Room with Number '" + room.getRoomNumber() + "' already exists!");
        }
        if (room.getOccupiedBeds() == null) {
            room.setOccupiedBeds(0);
        }
        room.updateAvailability();
        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Long id, Room roomDetails) {
        Room existingRoom = getRoomById(id);

        if (!existingRoom.getRoomNumber().equalsIgnoreCase(roomDetails.getRoomNumber())) {
            if (roomRepository.existsByRoomNumber(roomDetails.getRoomNumber())) {
                throw new DuplicateResourceException("Room with Number '" + roomDetails.getRoomNumber() + "' already exists!");
            }
        }

        existingRoom.setRoomNumber(roomDetails.getRoomNumber());
        existingRoom.setBuildingName(roomDetails.getBuildingName());
        existingRoom.setRoomType(roomDetails.getRoomType());
        if (roomDetails.getHostelType() != null) {
            existingRoom.setHostelType(roomDetails.getHostelType());
        }
        existingRoom.setCapacity(roomDetails.getCapacity());
        if (roomDetails.getOccupiedBeds() != null) {
            existingRoom.setOccupiedBeds(roomDetails.getOccupiedBeds());
        }
        existingRoom.setMonthlyRent(roomDetails.getMonthlyRent());
        existingRoom.updateAvailability();

        return roomRepository.save(existingRoom);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = getRoomById(id);
        roomRepository.delete(room);
    }

    public List<Room> filterRooms(String roomType, String status) {
        return filterRooms(roomType, status, "ALL");
    }

    public List<Room> filterRooms(String roomType, String status, String hostelType) {
        List<Room> all = getAllRooms();

        return all.stream()
                .filter(r -> {
                    if (roomType != null && !roomType.trim().isEmpty() && !roomType.equalsIgnoreCase("ALL")) {
                        if (!roomType.trim().equalsIgnoreCase(r.getRoomType())) return false;
                    }
                    if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
                        if (!status.trim().equalsIgnoreCase(r.getStatus())) return false;
                    }
                    if (hostelType != null && !hostelType.trim().isEmpty() && !hostelType.equalsIgnoreCase("ALL")) {
                        if (r.getHostelType() == null || !hostelType.trim().equalsIgnoreCase(r.getHostelType())) return false;
                    }
                    return true;
                })
                .toList();
    }

    public long countAvailableRooms() {
        return roomRepository.countByStatus("AVAILABLE");
    }

    public long countOccupiedRooms() {
        return roomRepository.countByStatus("FULL");
    }

    public long countTotalRooms() {
        return roomRepository.count();
    }
}
