package com.notification.service;

import com.notification.dto.NotificationRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void processNotification(NotificationRequestDTO request) {
        String eventType = request.getEventType() != null ? request.getEventType() : "BOOKING";

        log.info("================================================================================");
        log.info("[NOTIFICATION MICROSERVICE - PORT 8081] Simulated {} Alert Triggered!", eventType);
        log.info("Recipient Student : {}", request.getStudentName());
        log.info("Recipient Email   : {}", request.getStudentEmail());
        log.info("Room Number       : {}", request.getRoomNumber());
        log.info("Status            : {}", request.getBookingStatus());

        if (request.getFeeType() != null) {
            log.info("Fee Type          : {}", request.getFeeType());
        }
        if (request.getAmount() != null) {
            log.info("Amount            : ₹{}", request.getAmount());
        }
        if (request.getRemainingBalance() != null) {
            log.info("Remaining Balance : ₹{}", request.getRemainingBalance());
        }
        if (request.getReceiptNumber() != null) {
            log.info("Receipt Number    : {}", request.getReceiptNumber());
        }
        if (request.getMessage() != null) {
            log.info("Message Content   : {}", request.getMessage());
        } else {
            log.info("Message Content   : Dear {}, alert for status [{}] on Room {}",
                    request.getStudentName(), request.getBookingStatus(), request.getRoomNumber());
        }
        log.info("================================================================================");
    }
}
