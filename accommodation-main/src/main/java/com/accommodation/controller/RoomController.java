package com.accommodation.controller;

import com.accommodation.entity.Room;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(@RequestParam(value = "roomType", required = false) String roomType,
                            @RequestParam(value = "status", required = false) String status,
                            @RequestParam(value = "hostelType", required = false) String hostelType,
                            Model model) {
        List<Room> rooms = roomService.filterRooms(roomType, status, hostelType);
        model.addAttribute("rooms", rooms);
        model.addAttribute("selectedType", roomType != null ? roomType : "ALL");
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        model.addAttribute("selectedHostel", hostelType != null ? hostelType : "ALL");
        return "rooms/list";
    }

    @GetMapping("/new")
    public String showAddRoomForm(Model model) {
        if (!model.containsAttribute("room")) {
            Room room = new Room();
            room.setCapacity(1);
            room.setOccupiedBeds(0);
            room.setRoomType("Single");
            room.setHostelType("BOYS");
            model.addAttribute("room", room);
        }
        return "rooms/form";
    }

    @PostMapping
    public String addRoom(@Valid @ModelAttribute("room") Room room,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {
        if (room.getOccupiedBeds() != null && room.getCapacity() != null && room.getOccupiedBeds() > room.getCapacity()) {
            result.rejectValue("occupiedBeds", "invalidOccupancy", "Occupied beds cannot exceed room capacity!");
        }

        if (result.hasErrors()) {
            return "rooms/form";
        }

        try {
            roomService.addRoom(room);
            redirectAttributes.addFlashAttribute("successMessage", "Room '" + room.getRoomNumber() + "' added successfully!");
            return "redirect:/rooms";
        } catch (DuplicateResourceException ex) {
            result.rejectValue("roomNumber", "duplicate", ex.getMessage());
            return "rooms/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditRoomForm(@PathVariable("id") Long id, Model model) {
        Room room = roomService.getRoomById(id);
        model.addAttribute("room", room);
        model.addAttribute("isEdit", true);
        return "rooms/form";
    }

    @PostMapping("/{id}/edit")
    public String updateRoom(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("room") Room room,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (room.getOccupiedBeds() != null && room.getCapacity() != null && room.getOccupiedBeds() > room.getCapacity()) {
            result.rejectValue("occupiedBeds", "invalidOccupancy", "Occupied beds cannot exceed room capacity!");
        }

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "rooms/form";
        }

        try {
            roomService.updateRoom(id, room);
            redirectAttributes.addFlashAttribute("successMessage", "Room details updated successfully!");
            return "redirect:/rooms";
        } catch (DuplicateResourceException ex) {
            result.rejectValue("roomNumber", "duplicate", ex.getMessage());
            model.addAttribute("isEdit", true);
            return "rooms/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteRoom(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete room: " + ex.getMessage());
        }
        return "redirect:/rooms";
    }
}
