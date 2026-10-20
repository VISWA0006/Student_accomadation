package com.accommodation.controller;

import com.accommodation.entity.InAppNotification;
import com.accommodation.entity.NotificationCategory;
import com.accommodation.entity.Student;
import com.accommodation.service.InAppNotificationService;
import com.accommodation.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationCenterController {

    private final InAppNotificationService notificationService;
    private final StudentService studentService;

    public NotificationCenterController(InAppNotificationService notificationService,
                                        StudentService studentService) {
        this.notificationService = notificationService;
        this.studentService = studentService;
    }

    /**
     * Notification Center Page.
     * Displays alerts tailored to the current active persona (Student or Admin).
     */
    @GetMapping
    public String showNotificationCenter(@RequestParam(value = "category", required = false) NotificationCategory category,
                                         HttpSession session,
                                         Model model) {
        String currentRole = (String) session.getAttribute("currentRole");
        Long studentId = (Long) session.getAttribute("currentStudentId");
        boolean isStudent = "STUDENT".equalsIgnoreCase(currentRole);

        List<InAppNotification> notifications;
        long unreadCount;

        if (isStudent && studentId != null) {
            notifications = notificationService.getNotificationsForStudent(studentId, category);
            unreadCount = notificationService.getUnreadCountForStudent(studentId);
            Student student = studentService.getStudentById(studentId);
            model.addAttribute("personaName", student.getFullName());
        } else {
            notifications = notificationService.getNotificationsForAdmin(category);
            unreadCount = notificationService.getUnreadCountForAdmin();
            model.addAttribute("personaName", "Administrator");
        }

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", NotificationCategory.values());
        model.addAttribute("isStudent", isStudent);

        return "notifications/center";
    }

    /**
     * AJAX endpoint to mark a single notification as read without full page reload.
     */
    @PostMapping("/api/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markSingleAsRead(@PathVariable("id") Long id) {
        boolean updated = notificationService.markAsRead(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", updated);
        resp.put("id", id);
        return ResponseEntity.ok(resp);
    }

    /**
     * Endpoint to mark all notifications as read for current persona.
     */
    @PostMapping("/api/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(HttpSession session) {
        String currentRole = (String) session.getAttribute("currentRole");
        Long studentId = (Long) session.getAttribute("currentStudentId");

        if ("STUDENT".equalsIgnoreCase(currentRole) && studentId != null) {
            notificationService.markAllAsReadForStudent(studentId);
        } else {
            notificationService.markAllAsReadForAdmin();
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        return ResponseEntity.ok(resp);
    }

    /**
     * AJAX endpoint to poll or query unread notification count.
     */
    @GetMapping("/api/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(HttpSession session) {
        String currentRole = (String) session.getAttribute("currentRole");
        Long studentId = (Long) session.getAttribute("currentStudentId");

        long count = "STUDENT".equalsIgnoreCase(currentRole) && studentId != null
                ? notificationService.getUnreadCountForStudent(studentId)
                : notificationService.getUnreadCountForAdmin();

        Map<String, Object> resp = new HashMap<>();
        resp.put("unreadCount", count);
        return ResponseEntity.ok(resp);
    }
}
