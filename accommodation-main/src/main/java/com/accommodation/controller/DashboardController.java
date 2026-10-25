package com.accommodation.controller;

import com.accommodation.dto.FeeStatsDTO;
import com.accommodation.entity.HostelFee;
import com.accommodation.service.BookingService;
import com.accommodation.service.HostelFeeService;
import com.accommodation.service.RoomService;
import com.accommodation.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private final StudentService studentService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final HostelFeeService feeService;

    public DashboardController(StudentService studentService,
                               RoomService roomService,
                               BookingService bookingService,
                               HostelFeeService feeService) {
        this.studentService = studentService;
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.feeService = feeService;
    }

    @GetMapping({"/", "/dashboard"})
    public String viewDashboard(Model model) {
        model.addAttribute("totalStudents", studentService.getAllStudents().size());
        model.addAttribute("totalRooms", roomService.countTotalRooms());
        model.addAttribute("availableRooms", roomService.countAvailableRooms());
        model.addAttribute("occupiedRooms", roomService.countOccupiedRooms());
        model.addAttribute("totalBookings", bookingService.countTotalBookings());

        FeeStatsDTO feeStats = feeService.getFeeStats();
        List<HostelFee> overdueFees = feeService.getOverdueFees("ALL");
        model.addAttribute("feeStats", feeStats);
        model.addAttribute("overdueFees", overdueFees);
        model.addAttribute("today", LocalDate.now());

        model.addAttribute("recentBookings", bookingService.getAllBookings());
        model.addAttribute("recentStudents", studentService.getAllStudents());

        return "dashboard";
    }
}
