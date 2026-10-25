package com.accommodation.controller;

import com.accommodation.dto.BookingRequestDTO;
import com.accommodation.entity.Booking;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.exception.ResourceNotFoundException;
import com.accommodation.exception.RoomNotAvailableException;
import com.accommodation.service.AccommodationStayService;
import com.accommodation.service.BookingService;
import com.accommodation.service.RoomService;
import com.accommodation.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final StudentService studentService;
    private final RoomService roomService;
    private final AccommodationStayService stayService;

    public BookingController(BookingService bookingService,
                             StudentService studentService,
                             RoomService roomService,
                             AccommodationStayService stayService) {
        this.bookingService = bookingService;
        this.studentService = studentService;
        this.roomService = roomService;
        this.stayService = stayService;
    }

    @GetMapping
    public String listBookings(@RequestParam(value = "status", required = false) String status, Model model) {
        List<Booking> bookings = bookingService.getBookingsByStatus(status);
        model.addAttribute("bookings", bookings);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");
        return "bookings/list";
    }

    @GetMapping("/new")
    public String showNewBookingForm(Model model) {
        if (!model.containsAttribute("bookingRequest")) {
            model.addAttribute("bookingRequest", new BookingRequestDTO());
        }
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("availableRooms", roomService.getAvailableRooms());
        return "bookings/form";
    }

    @PostMapping
    public String processBooking(@Valid @ModelAttribute("bookingRequest") BookingRequestDTO bookingRequest,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("students", studentService.getAllStudents());
            model.addAttribute("availableRooms", roomService.getAvailableRooms());
            return "bookings/form";
        }

        try {
            bookingService.createBooking(bookingRequest);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Accommodation room booked successfully.");
            return "redirect:/bookings";
        } catch (DuplicateResourceException | RoomNotAvailableException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("students", studentService.getAllStudents());
            model.addAttribute("availableRooms", roomService.getAvailableRooms());
            return "bookings/form";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking #" + id + " has been cancelled. Bed freed up!");
        } catch (IllegalStateException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not cancel booking: " + ex.getMessage());
        }
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/check-in")
    public String checkIn(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            stayService.checkIn(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student checked in.");
        } catch (IllegalStateException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bookings";
    }

    @PostMapping("/{id}/check-out")
    public String checkOut(@PathVariable Long id,
                           @RequestParam(required = false) String note,
                           RedirectAttributes redirectAttributes) {
        try {
            stayService.checkOut(id, note);
            redirectAttributes.addFlashAttribute("successMessage", "Student checked out.");
        } catch (IllegalStateException | ResourceNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bookings";
    }
}
