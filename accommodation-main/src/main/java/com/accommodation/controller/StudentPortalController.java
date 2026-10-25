package com.accommodation.controller;

import com.accommodation.entity.HostelFee;
import com.accommodation.entity.PaymentTransaction;
import com.accommodation.entity.Student;
import com.accommodation.service.HostelFeeService;
import com.accommodation.service.StudentService;
import com.accommodation.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentPortalController {

    private final HostelFeeService feeService;
    private final StudentService studentService;
    private final UserAccountService userAccountService;

    public StudentPortalController(HostelFeeService feeService, StudentService studentService,
                                   UserAccountService userAccountService) {
        this.feeService = feeService;
        this.studentService = studentService;
        this.userAccountService = userAccountService;
    }

    @GetMapping("/fees")
    public String studentFeeDashboard(@AuthenticationPrincipal UserDetails principal, Model model) {
        Long studentId = userAccountService.getStudentId(principal.getUsername());
        Student student = studentService.getStudentById(studentId);
        List<HostelFee> fees = feeService.getFeesByStudent(studentId);
        List<PaymentTransaction> transactions = feeService.getTransactionsForStudent(studentId);

        Double totalFees = fees.stream().map(HostelFee::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add).doubleValue();
        Double totalPaid = fees.stream().map(HostelFee::getPaidAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add).doubleValue();
        Double totalBalance = fees.stream().map(HostelFee::getRemainingBalance)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add).doubleValue();

        LocalDate nextDueDate = fees.stream()
                .filter(f -> f.getRemainingBalance().signum() > 0)
                .map(HostelFee::getDueDate)
                .min(Comparator.naturalOrder())
                .orElse(null);

        model.addAttribute("student", student);
        model.addAttribute("fees", fees);
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalFees", Math.round(totalFees * 100.0) / 100.0);
        model.addAttribute("totalPaid", Math.round(totalPaid * 100.0) / 100.0);
        model.addAttribute("totalBalance", Math.round(totalBalance * 100.0) / 100.0);
        model.addAttribute("nextDueDate", nextDueDate);
        model.addAttribute("today", LocalDate.now());
        return "student/dashboard";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "student/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails principal,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            userAccountService.changePassword(principal.getUsername(), currentPassword, newPassword);
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return "redirect:/login?passwordChanged";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/change-password";
        }
    }
}
