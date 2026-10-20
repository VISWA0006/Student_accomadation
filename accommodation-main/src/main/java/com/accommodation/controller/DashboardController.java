package com.accommodation.controller;

import com.accommodation.service.BookingService;
import com.accommodation.service.RoomService;
import com.accommodation.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final StudentService studentService;
    private final RoomService roomService;
    private final BookingService bookingService;

    public DashboardController(StudentService studentService, RoomService roomService, BookingService bookingService) {
        this.studentService = studentService;
        this.roomService = roomService;
        this.bookingService = bookingService;
    }

    @GetMapping({"/", "/dashboard"})
    public String viewDashboard(Model model) {
        model.addAttribute("totalStudents", studentService.getAllStudents().size());
        model.addAttribute("totalRooms", roomService.countTotalRooms());
        model.addAttribute("availableRooms", roomService.countAvailableRooms());
        model.addAttribute("occupiedRooms", roomService.countOccupiedRooms());
        model.addAttribute("totalBookings", bookingService.countTotalBookings());

        // Recent listings for quick overview
        model.addAttribute("recentBookings", bookingService.getAllBookings());
        model.addAttribute("recentStudents", studentService.getAllStudents());

        return "dashboard";
    }
}
