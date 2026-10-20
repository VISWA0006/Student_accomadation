package com.notification.service;

import com.notification.dto.NotificationRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void processNotification(NotificationRequestDTO request) {
        log.info("================================================================================");
        log.info("[NOTIFICATION MICROSERVICE - PORT 8081] Simulated Booking Alert Triggered!");
        log.info("Recipient Student : {}", request.getStudentName());
        log.info("Recipient Email   : {}", request.getStudentEmail());
        log.info("Allocated Room    : Room {}", request.getRoomNumber());
        log.info("Booking Status    : {}", request.getBookingStatus());
        log.info("Message           : Dear {}, your college accommodation booking for Room {} has been confirmed!",
                request.getStudentName(), request.getRoomNumber());
        log.info("================================================================================");
    }
}
