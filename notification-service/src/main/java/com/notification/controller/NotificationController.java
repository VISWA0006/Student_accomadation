package com.notification.controller;

import com.notification.dto.NotificationRequestDTO;
import com.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/booking")
    public ResponseEntity<Map<String, Object>> handleBookingNotification(
            @Valid @RequestBody NotificationRequestDTO request) {

        notificationService.processNotification(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("message", "Simulated booking notification logged successfully by Notification Service");
        response.put("studentEmail", request.getStudentEmail());
        response.put("roomNumber", request.getRoomNumber());

        return ResponseEntity.ok(response);
    }
}
