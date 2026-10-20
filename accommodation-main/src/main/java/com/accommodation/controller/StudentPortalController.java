package com.accommodation.controller;

import com.accommodation.entity.HostelFee;
import com.accommodation.entity.PaymentTransaction;
import com.accommodation.entity.Student;
import com.accommodation.service.HostelFeeService;
import com.accommodation.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    public StudentPortalController(HostelFeeService feeService, StudentService studentService) {
        this.feeService = feeService;
        this.studentService = studentService;
    }

    /**
     * Persona switcher for testing and demonstration.
     * Switches the current session role between ADMIN, WARDEN, and specific Students.
     */
    @GetMapping("/switch")
    public String switchPersona(@RequestParam("role") String role,
                                @RequestParam(value = "studentId", required = false) Long studentId,
                                @RequestParam(value = "redirect", defaultValue = "/dashboard") String redirect,
                                HttpSession session) {
        session.setAttribute("currentRole", role.toUpperCase());
        if ("STUDENT".equalsIgnoreCase(role) && studentId != null) {
            session.setAttribute("currentStudentId", studentId);
            return "redirect:/student/fees";
        } else {
            session.removeAttribute("currentStudentId");
            return "redirect:" + redirect;
        }
    }

    /**
     * Dedicated Student Fee Portal Dashboard.
     * Enforces that students only see their own financial data and payment receipts.
     */
    @GetMapping("/fees")
    public String studentFeeDashboard(HttpSession session, Model model) {
        Long studentId = (Long) session.getAttribute("currentStudentId");
        if (studentId == null) {
            // Default to first student if not set
            List<Student> allStudents = studentService.getAllStudents();
            if (!allStudents.isEmpty()) {
                studentId = allStudents.get(0).getId();
                session.setAttribute("currentStudentId", studentId);
            } else {
                return "redirect:/dashboard";
            }
        }

        Student student = studentService.getStudentById(studentId);
        List<HostelFee> fees = feeService.getFeesByStudent(studentId);
        List<PaymentTransaction> transactions = feeService.getTransactionsForStudent(studentId);

        Double totalFees = fees.stream().mapToDouble(HostelFee::getTotalAmount).sum();
        Double totalPaid = fees.stream().mapToDouble(HostelFee::getPaidAmount).sum();
        Double totalBalance = fees.stream().mapToDouble(HostelFee::getRemainingBalance).sum();

        // Find earliest upcoming or overdue due date
        LocalDate nextDueDate = fees.stream()
                .filter(f -> f.getRemainingBalance() > 0)
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
        model.addAttribute("allStudents", studentService.getAllStudents());

        return "student/dashboard";
    }
}
