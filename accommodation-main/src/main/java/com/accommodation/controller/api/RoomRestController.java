package com.accommodation.controller.api;

import com.accommodation.dto.RoomDTO;
import com.accommodation.entity.Room;
import com.accommodation.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

    private final RoomService roomService;

    public RoomRestController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        List<RoomDTO> dtos = roomService.getAllRooms().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/available")
    public ResponseEntity<List<RoomDTO>> getAvailableRooms() {
        List<RoomDTO> dtos = roomService.getAvailableRooms().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable("id") Long id) {
        Room room = roomService.getRoomById(id);
        return ResponseEntity.ok(toDTO(room));
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@Valid @RequestBody RoomDTO roomDTO) {
        Room room = toEntity(roomDTO);
        Room savedRoom = roomService.addRoom(room);
        return new ResponseEntity<>(toDTO(savedRoom), HttpStatus.CREATED);
    }

    private RoomDTO toDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getRoomNumber(),
                room.getBuildingName(),
                room.getRoomType(),
                room.getCapacity(),
                room.getOccupiedBeds(),
                room.getMonthlyRent(),
                room.getHostelType(),
                room.getStatus(),
                room.getAvailableBeds()
        );
    }

    private Room toEntity(RoomDTO dto) {
        Room room = new Room();
        room.setRoomNumber(dto.getRoomNumber());
        room.setBuildingName(dto.getBuildingName());
        room.setRoomType(dto.getRoomType());
        if (dto.getHostelType() != null) {
            room.setHostelType(dto.getHostelType());
        }
        room.setCapacity(dto.getCapacity());
        room.setOccupiedBeds(dto.getOccupiedBeds() != null ? dto.getOccupiedBeds() : 0);
        room.setMonthlyRent(dto.getMonthlyRent());
        room.updateAvailability();
        return room;
    }
}
