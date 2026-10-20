package com.accommodation.service;

import com.accommodation.dto.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Client service to communicate with the Accommodation Notification Microservice (Port 8081).
 * Demonstrates Inter-service REST communication and graceful fault tolerance.
 */
@Service
public class NotificationClientService {

    private static final Logger log = LoggerFactory.getLogger(NotificationClientService.class);

    private final RestClient restClient;
    private final String notificationUrl;

    public NotificationClientService(
            @Value("${notification.service.url:http://localhost:8081/api/notifications/booking}") String notificationUrl) {
        this.notificationUrl = notificationUrl;
        this.restClient = RestClient.builder().build();
    }

    /**
     * Sends booking details to the Notification Microservice.
     */
    public boolean sendBookingNotification(NotificationPayload payload) {
        return sendNotification(payload);
    }

    /**
     * Sends any notification (booking, fee reminder, payment confirmation) to the microservice.
     * Fails gracefully without failing the database transaction.
     */
    public boolean sendNotification(NotificationPayload payload) {
        try {
            log.info("Sending [{}] notification to microservice at {}: student={}, room={}",
                    payload.getEventType(), notificationUrl, payload.getStudentName(), payload.getRoomNumber());

            String response = restClient.post()
                    .uri(notificationUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

            log.info("Microservice response: {}", response);
            return true;
        } catch (Exception ex) {
            log.warn("Notification Service is unavailable or returned an error: {}. " +
                     "Main transaction remains saved successfully (Graceful Degradation).", ex.getMessage());
            return false;
        }
    }
}
