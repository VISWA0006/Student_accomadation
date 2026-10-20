# Student Accommodation Management System - Architecture Guide

## 1. Overview
The **Student Accommodation Management System** is built using modern **Spring Boot 3** and **Java 21/24**, structured following enterprise MVC design principles and a decoupled microservices paradigm.

```
                           [ Web Browser / User ]
                                     |
                                     | HTTP Requests (Port 8080)
                                     v
                 +---------------------------------------+
                 |          accommodation-main           |
                 |---------------------------------------|
                 | - Spring MVC Web Controllers          |
                 |   (Dashboard, Students, Rooms,        |
                 |    Bookings)                          |
                 | - Spring REST Controllers             |
                 |   (/api/students, /api/rooms,         |
                 |    /api/bookings)                     |
                 | - Spring AOP Logging Aspect           |
                 | - Service Layer (Transactional)       |
                 | - Spring Data JPA & Hibernate ORM     |
                 +-------------------+-------------------+
                                     |
                    +----------------+----------------+
                    |                                 |
                    v (JDBC Port 5432)                v (HTTP REST Port 8081)
         +--------------------+            +------------------------+
         | PostgreSQL Database|            | notification-service   |
         | (accommodation_db) |            |------------------------|
         +--------------------+            | - Notification API     |
                                           | - Simulated Console    |
                                           |   Booking Alerts       |
                                           +------------------------+
```

---

## 2. Layered Architecture (MVC)

### A. Presentation Layer (View & Controllers)
- **Thymeleaf Template Engine**: Server-rendered, HTML5-compliant responsive UI templates located under `src/main/resources/templates/`.
- **Spring MVC `@Controller`**: Handles HTML browser page navigation, form validation binding (`BindingResult`), and flash messages (`RedirectAttributes`).
- **Spring `@RestController`**: Provides standard JSON REST endpoints for external consumers (e.g., Postman, mobile apps).

### B. Business Logic Layer (Service)
- **`@Service` Components**: Encapsulates all domain business rules.
- **`@Transactional` Management**: Guarantees atomic updates when booking or cancelling rooms (e.g. updating room beds and creating booking records simultaneously).
- **Graceful Fault Tolerance**: Inter-service communication with the Notification service is wrapped in exception handling so that notification service downtime does not abort database transactions.

### C. Data Access Layer (Repository)
- **Spring Data JPA (`@Repository`)**: Eliminates boilerplate DAO code by extending `JpaRepository<T, ID>`.
- **HQL (Hibernate Query Language)**: Custom queries written using `@Query` operating on Entity classes and fields rather than native database tables.

### D. Cross-Cutting Concerns (Spring AOP)
- **`@Aspect` & `@Around`**: Intercepts service method executions to benchmark execution duration and log method entries and exits.

---

## 3. Monolith vs. Microservices Architecture

| Architectural Feature | Monolithic Architecture | Microservices Architecture (Used Here) |
| :--- | :--- | :--- |
| **Codebase** | Single shared codebase for all features. | Independent projects (`accommodation-main` and `notification-service`). |
| **Deployment** | Entire app deployed as one JAR/WAR. | Deployed and scaled independently across separate JVMs/ports (8080 and 8081). |
| **Fault Isolation** | A crash in notification module halts everything. | If notification service fails, core accommodation bookings still succeed. |
| **Technology Flexibility** | Locked into one tech stack. | Notification service could be written in Go, Node.js, or another version of Java. |
| **Complexity** | Easier local setup; simple debugging. | Requires network communication, service discovery, and resilient error handling. |

---

## 4. Inter-Service Communication
The primary service communicates with the notification microservice using Spring 6's modern **`RestClient`** over HTTP:
- Target: `POST http://localhost:8081/api/notifications/booking`
- Payload: JSON with `studentName`, `studentEmail`, `roomNumber`, and `bookingStatus`.
- Fallback: Catch block logs warnings and continues normal execution without failing the user's booking.
