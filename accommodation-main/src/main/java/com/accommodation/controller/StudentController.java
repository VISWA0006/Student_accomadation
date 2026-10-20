package com.accommodation.controller;

import com.accommodation.entity.Student;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.service.BookingService;
import com.accommodation.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final BookingService bookingService;

    public StudentController(StudentService studentService, BookingService bookingService) {
        this.studentService = studentService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String listStudents(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Student> students = studentService.searchStudents(keyword);
        model.addAttribute("students", students);
        model.addAttribute("keyword", keyword);
        return "students/list";
    }

    @GetMapping("/new")
    public String showAddStudentForm(Model model) {
        if (!model.containsAttribute("student")) {
            model.addAttribute("student", new Student());
        }
        return "students/form";
    }

    @PostMapping
    public String addStudent(@Valid @ModelAttribute("student") Student student,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "students/form";
        }
        try {
            studentService.addStudent(student);
            redirectAttributes.addFlashAttribute("successMessage", "Student '" + student.getFullName() + "' added successfully!");
            return "redirect:/students";
        } catch (DuplicateResourceException ex) {
            result.rejectValue("registerNumber", "duplicate", ex.getMessage());
            return "students/form";
        }
    }

    @GetMapping("/{id}")
    public String viewStudentDetails(@PathVariable("id") Long id, Model model) {
        Student student = studentService.getStudentById(id);
        model.addAttribute("student", student);
        model.addAttribute("activeBookings", bookingService.getActiveBookingsByStudentHql(id));
        return "students/details";
    }

    @GetMapping("/{id}/edit")
    public String showEditStudentForm(@PathVariable("id") Long id, Model model) {
        Student student = studentService.getStudentById(id);
        model.addAttribute("student", student);
        model.addAttribute("isEdit", true);
        return "students/form";
    }

    @PostMapping("/{id}/edit")
    public String updateStudent(@PathVariable("id") Long id,
                                @Valid @ModelAttribute("student") Student student,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "students/form";
        }
        try {
            studentService.updateStudent(id, student);
            redirectAttributes.addFlashAttribute("successMessage", "Student details updated successfully!");
            return "redirect:/students";
        } catch (DuplicateResourceException ex) {
            result.rejectValue("registerNumber", "duplicate", ex.getMessage());
            model.addAttribute("isEdit", true);
            return "students/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteStudent(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student deleted successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete student: " + ex.getMessage());
        }
        return "redirect:/students";
    }
}
