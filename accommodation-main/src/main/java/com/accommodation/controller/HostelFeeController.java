package com.accommodation.controller;

import com.accommodation.dto.FeeFilterDTO;
import com.accommodation.dto.FeeStatsDTO;
import com.accommodation.dto.HostelFeeRequestDTO;
import com.accommodation.dto.RecordPaymentDTO;
import com.accommodation.entity.FeeType;
import com.accommodation.entity.HostelFee;
import com.accommodation.entity.PaymentStatus;
import com.accommodation.entity.PaymentTransaction;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.service.FeeReminderSchedulerService;
import com.accommodation.service.HostelFeeService;
import com.accommodation.service.RoomService;
import com.accommodation.service.StudentService;
import com.accommodation.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/fees")
public class HostelFeeController {

    private final HostelFeeService feeService;
    private final StudentService studentService;
    private final RoomService roomService;
    private final FeeReminderSchedulerService reminderSchedulerService;
    private final UserAccountService userAccountService;
    private static final int FEE_PAGE_SIZE = 25;

    public HostelFeeController(HostelFeeService feeService,
                               StudentService studentService,
                               RoomService roomService,
                               FeeReminderSchedulerService reminderSchedulerService,
                               UserAccountService userAccountService) {
        this.feeService = feeService;
        this.studentService = studentService;
        this.roomService = roomService;
        this.reminderSchedulerService = reminderSchedulerService;
        this.userAccountService = userAccountService;
    }

    /**
     * Admin / Warden Hostel Fee Management List with Multi-Criteria Filtering.
     */
    @GetMapping
    public String listFees(@ModelAttribute("filter") FeeFilterDTO filter,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        Page<HostelFee> feePage = feeService.getFeePage(filter, PageRequest.of(Math.max(0, page), FEE_PAGE_SIZE));
        FeeStatsDTO stats = feeService.getFeeStats();

        model.addAttribute("fees", feePage.getContent());
        model.addAttribute("feePage", feePage);
        model.addAttribute("currentPage", feePage.getNumber());
        model.addAttribute("totalPages", feePage.getTotalPages());
        model.addAttribute("stats", stats);
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("filter", filter);
        return "fees/list";
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportFeesCsv() {
        StringBuilder csv = new StringBuilder(
                "Fee ID,Student Register Number,Student Name,Room,Fee Type,Total Amount,Paid Amount,Balance,Status,Due Date\r\n");
        for (HostelFee fee : feeService.getAllFees()) {
            appendCsvRow(csv, fee.getId(), fee.getStudent().getRegisterNumber(),
                    fee.getStudent().getFullName(), fee.getRoomNumber(),
                    fee.getFeeType().getDisplayName(), fee.getTotalAmount(),
                    fee.getPaidAmount(), fee.getRemainingBalance(), fee.getStatus(),
                    fee.getDueDate());
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hostel-fees.csv\"")
                .contentType(new MediaType("text", "csv"))
                .body(csv.toString());
    }

    private void appendCsvRow(StringBuilder csv, Object... values) {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            String value = values[i] == null ? "" : values[i].toString();
            if (!value.isEmpty() && "=+-@\t\r\n".indexOf(value.charAt(0)) >= 0) {
                value = "'" + value;
            }
            csv.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
        csv.append("\r\n");
    }

    /**
     * Dedicated Overdue Fee Tracking & Summary Dashboard.
     */
    @GetMapping("/overdue")
    public String overdueDashboard(@RequestParam(value = "duration", defaultValue = "ALL") String duration,
                                   Model model) {
        List<HostelFee> overdueFees = feeService.getOverdueFees(duration);
        FeeStatsDTO stats = feeService.getFeeStats();

        model.addAttribute("overdueFees", overdueFees);
        model.addAttribute("stats", stats);
        model.addAttribute("selectedDuration", duration);
        model.addAttribute("today", LocalDate.now());
        return "fees/overdue";
    }

    /**
     * Show form to assign a new fee to a student.
     */
    @GetMapping("/new")
    public String showNewFeeForm(Model model) {
        if (!model.containsAttribute("feeRequest")) {
            model.addAttribute("feeRequest", new HostelFeeRequestDTO());
        }
        model.addAttribute("students", studentService.getAllStudents());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("feeTypes", FeeType.values());
        return "fees/form";
    }

    /**
     * Save new fee assignment.
     */
    @PostMapping
    public String createFee(@Valid @ModelAttribute("feeRequest") HostelFeeRequestDTO request,
                            BindingResult result,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("students", studentService.getAllStudents());
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("feeTypes", FeeType.values());
            return "fees/form";
        }

        try {
            HostelFee created = feeService.createFee(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Hostel fee of ₹" + created.getTotalAmount() + " successfully assigned to " +
                    created.getStudent().getFullName() + " (" + created.getFeeType().getDisplayName() + ")!");
            return "redirect:/fees/" + created.getId();
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("students", studentService.getAllStudents());
            model.addAttribute("rooms", roomService.getAllRooms());
            model.addAttribute("feeTypes", FeeType.values());
            return "fees/form";
        }
    }

    /**
     * Fee Details, Payment History, and Payment Submission Form.
     */
    @GetMapping("/{id}")
    public String feeDetails(@PathVariable("id") Long id, Model model) {
        HostelFee fee = feeService.getFeeById(id);
        List<PaymentTransaction> transactions = feeService.getTransactionsForFee(id);

        if (!model.containsAttribute("paymentDTO")) {
            RecordPaymentDTO paymentDTO = new RecordPaymentDTO();
            paymentDTO.setAmount(fee.getRemainingBalance());
            model.addAttribute("paymentDTO", paymentDTO);
        }

        model.addAttribute("fee", fee);
        model.addAttribute("transactions", transactions);
        model.addAttribute("today", LocalDate.now());
        return "fees/details";
    }

    /**
     * Record a payment against a hostel fee.
     */
    @PostMapping("/{id}/pay")
    public String recordPayment(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("paymentDTO") RecordPaymentDTO paymentDTO,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            HostelFee fee = feeService.getFeeById(id);
            model.addAttribute("fee", fee);
            model.addAttribute("transactions", feeService.getTransactionsForFee(id));
            model.addAttribute("today", LocalDate.now());
            return "fees/details";
        }

        try {
            PaymentTransaction transaction = feeService.recordPayment(id, paymentDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment of ₹" + transaction.getAmount() + " successfully recorded! Receipt Number: " +
                    transaction.getReceiptNumber());
            return "redirect:/fees/receipts/" + transaction.getReceiptNumber();
        } catch (DuplicateResourceException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/fees/" + id;
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/fees/" + id;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error recording payment: " + ex.getMessage());
            return "redirect:/fees/" + id;
        }
    }

    /**
     * Printable Official Payment Receipt.
     */
    @GetMapping("/receipts/{receiptNumber}")
    public String viewReceipt(@PathVariable("receiptNumber") String receiptNumber, Model model,
                              @AuthenticationPrincipal UserDetails principal) {
        PaymentTransaction transaction = feeService.getTransactionByReceipt(receiptNumber);
        if (userAccountService.isStudentAccount(principal.getUsername())
                && !transaction.getStudent().getId().equals(
                userAccountService.getStudentId(principal.getUsername()))) {
            throw new AccessDeniedException("You may only view your own payment receipts.");
        }
        model.addAttribute("transaction", transaction);
        model.addAttribute("fee", transaction.getHostelFee());
        model.addAttribute("student", transaction.getStudent());
        return "fees/receipt";
    }

    /**
     * Manually trigger the scheduled reminder job for live testing.
     */
    @PostMapping("/run-reminders")
    public String runRemindersManual(RedirectAttributes redirectAttributes) {
        int count = reminderSchedulerService.runFeeAuditAndReminders();
        redirectAttributes.addFlashAttribute("successMessage",
                "Automated fee reminder scan executed successfully! " + count + " notification alerts processed.");
        return "redirect:/fees/overdue";
    }
}
