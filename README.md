# Student Accommodation Management System
> A full-stack college accommodation application built with **Spring Boot 3**, **Spring MVC**, **Spring Data JPA / Hibernate**, **PostgreSQL**, **Thymeleaf**, and **Spring AOP**.

---

## 1. Project Overview

The **Student Accommodation Management System** streamlines college hostel and dorm administration. It enables hostel administrators to manage student registrations, oversee room bed capacities, process room allocations within ACID database transactions, prevent duplicate active allocations, and provide durable in-app notifications.

---

## 2. Technology Stack

* **Backend:** Java 21, Spring Boot 3.3.5, Spring MVC, Spring Data JPA, Hibernate ORM
* **Aspect-Oriented Programming:** Spring AOP (Execution benchmarking & logging)
* **Frontend:** Thymeleaf template engine, HTML5, Vanilla CSS3 (modern glassmorphism design system), JavaScript
* **Database:** PostgreSQL (with H2 in-memory profile available for zero-config offline testing)
* **Notifications:** Persistent in-app notifications with unread filtering and deduplication
* **Build & Tools:** Apache Maven, JUnit 5, Mockito, MockMvc, Postman
* **Version Control & DevOps:** Git, GitHub, GitHub Actions CI/CD pipeline

---

## 3. Project Architecture & Directory Structure

```
c:/Student_accomadation/
├── pom.xml                                   # Root Maven parent aggregator
├── .gitignore                                # Git ignore configuration
├── db-setup.sql                              # PostgreSQL database and table setup script
├── .github/workflows/maven.yml               # GitHub Actions CI workflow
├── accommodation-main/                       # Main Application (PostgreSQL: 8085; H2: 8080)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/accommodation/
│       │   │   ├── AccommodationApplication.java
│       │   │   ├── aspect/LoggingAspect.java
│       │   │   ├── config/DataInitializer.java
│       │   │   ├── controller/
│       │   │   │   ├── DashboardController.java
│       │   │   │   ├── StudentController.java
│       │   │   │   ├── RoomController.java
│       │   │   │   ├── BookingController.java
│       │   │   │   ├── HostelFeeController.java
│       │   │   │   ├── StudentPortalController.java
│       │   │   │   ├── NotificationCenterController.java
│       │   │   │   └── api/ (StudentRestController, RoomRestController, BookingRestController)
│       │   │   ├── dto/ (BookingRequestDTO, HostelFeeRequestDTO, RecordPaymentDTO, FeeStatsDTO, FeeFilterDTO, NotificationPayload)
│       │   │   ├── entity/ (Student, Room, Booking, HostelFee, PaymentTransaction, InAppNotification, FeeType, PaymentStatus, NotificationCategory, Person, StudentPerson)
│       │   │   ├── exception/ (GlobalExceptionHandler, Custom Exceptions)
│       │   │   ├── repository/ (StudentRepository, RoomRepository, BookingRepository, HostelFeeRepository, PaymentTransactionRepository, InAppNotificationRepository)
│       │   │   └── service/ (StudentService, RoomService, BookingService, HostelFeeService, InAppNotificationService, FeeReminderSchedulerService, NotificationClientService)
│       │   └── resources/
│       │       ├── application.properties
│       │       ├── application-h2.properties
│       │       ├── static/ (css/style.css, js/main.js)
│       │       └── templates/ (dashboard.html, students/, rooms/, bookings/, fees/, student/, notifications/)
│       └── test/ (Unit and REST Integration tests)
└── notification-service/                     # Standalone sample service; not used for app notifications
    ├── pom.xml
    └── src/
        └── main/java/com/notification/
            ├── NotificationApplication.java
            ├── controller/NotificationController.java
            ├── dto/NotificationRequestDTO.java
            └── service/NotificationService.java
```

---

## 4. Key Application Features

### A. Dashboard Overview (`/dashboard`)
* Real-time stat counters: Total Students, Total Rooms, Available Rooms, Occupied Rooms, Total Bookings.
* Quick-access recent booking records with instant cancellation buttons.
* Enrolled student snapshot table.

### B. Student Management (`/students`)
* Full CRUD (Create, Read, Update, Delete) with form validation.
* Unique register number validation (prevents duplicate entries).
* Student gender assignment (`MALE` / `FEMALE`) for verified hostel allocation.
* Quick search by name or register number.
* Detailed profile view (`/students/{id}`) displaying active accommodation allocations retrieved via **HQL**.

### C. Room Management (`/rooms`)
* Track rooms across buildings, types (Single, Double Sharing, Triple Sharing), and monthly rent.
* **Hostel Splitting:** Segregation into **Boys Hostel** (`BOYS`), **Girls Hostel** (`GIRLS`), or **Co-Ed** (`COED`).
* Bed calculation: **Available Beds = Capacity - Occupied Beds**.
* Multi-criteria filtering by hostel type, room type, and availability status (`AVAILABLE` vs `FULL`).
* Automatic status transition to `FULL` when capacity is reached.

### D. Accommodation Booking & Transaction Management (`/bookings`)
* Allocate a student to an available room with interactive gender matching.
* Enforces business rules inside an **ACID `@Transactional`** block:
  1. **Gender Rule:** Male students can only be allocated to Boys Hostel; Female students to Girls Hostel.
  2. Room must have at least 1 free bed.
  3. Student cannot already hold an `ACTIVE` booking.
  4. Increments room occupancy and updates room status atomically.
  5. Stores a booking confirmation in the in-app notification center.
* Booking confirmations are stored in the in-app notification center.
* Admins can record move-in and move-out dates and an optional move-out note.

### E. Fee Reports
* Admins can export fee records as CSV from `/fees/export.csv`.
* Fee records are filtered and paginated in the database; dashboard totals use aggregate queries.
* Booking, fee assignment, payment, and reminder notifications stay in the app inbox; no email or SMS is sent.

### F. Accounts & Security
* Admins sign in with configured credentials. Admins can provision student accounts from student details; the one-time temporary password must be changed at first sign-in.
* Student pages and receipts are restricted to the authenticated student's own data.

### E. Academic Requirements Demonstrated
1. **Hibernate Query Language (HQL):**
   - Query 1: `SELECT s FROM Student s WHERE LOWER(s.department) = LOWER(:dept)`
   - Query 2: `SELECT b FROM Booking b WHERE b.student.id = :studentId AND b.status = 'ACTIVE'`
2. **JPA Inheritance Mapping:**
   - Base entity `Person` and subclass `StudentPerson` using `@Inheritance(strategy = InheritanceType.JOINED)`.
3. **Spring AOP:**
   - `LoggingAspect` intercepting all service methods with `@Around` advice to benchmark execution time.
4. **Schema evolution:** Flyway applies versioned migrations; Hibernate validates the resulting schema.

---

## 5. Prerequisites & Local Setup

### Prerequisites
* **Java:** JDK 21 installed (`java -version`)
* **Maven:** Apache Maven 3.8+ installed (`mvn -version`)
* **PostgreSQL:** PostgreSQL 14+ installed and running on port 5432

---

## 6. Database Setup

1. Create the PostgreSQL database:
```sql
CREATE DATABASE accommodation_db;
```
2. Configure connection credentials through `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. PostgreSQL startup also requires `ADMIN_USERNAME` and `ADMIN_PASSWORD`.

Flyway manages schema changes. Existing non-empty databases are baselined at version 1 and upgraded without recreating tables or records. Back up the database before upgrading. The upgrade requires each student to have at most one active booking; resolve existing duplicates before applying it.

> **Offline demo:** Use the `h2` profile. Demo administrator credentials are `admin` / `DemoAdmin123!`; override with `ADMIN_USERNAME` and `ADMIN_PASSWORD`. H2 is in-memory, so its data is lost when the app stops.

---

## 7. How to Run the Application

From the project root, start the PostgreSQL-backed application:
```powershell
mvn -f accommodation-main\pom.xml spring-boot:run
```

The PostgreSQL profile listens on port **8085**. To use the H2 demo profile:
```powershell
mvn -f accommodation-main\pom.xml spring-boot:run "-Dspring-boot.run.profiles=h2"
```

H2 listens on port **8080** by default. If that port is occupied, pass a free port, for example `"-Dspring-boot.run.arguments=--server.port=8086"`.

Open `/login` at `http://localhost:8085` (PostgreSQL) or `http://localhost:8080` (H2). Main pages include `/dashboard`, `/fees`, `/fees/export.csv`, `/student/fees`, and `/notifications`.

After creating a student record, open its details page and provision an account. Share its temporary password securely; students must change it at first sign-in.

---

## 8. Running Automated Tests

Run tests for both services directly using Maven:
```powershell
# Run all tests in accommodation-main (9 tests including Unit and REST Integration)
mvn -f accommodation-main/pom.xml test

# Run microservice tests
mvn -f notification-service/pom.xml test
```

---

## 9. REST API Summary

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/students` | Get all students (JSON) |
| `GET` | `/api/students/{id}` | Get student by ID |
| `POST` | `/api/students` | Add new student |
| `PUT` | `/api/students/{id}` | Update student |
| `DELETE` | `/api/students/{id}` | Delete student |
| `GET` | `/api/students/department/{dept}` | **HQL Query 1:** Filter students by department |
| `GET` | `/api/rooms` | Get all rooms |
| `GET` | `/api/rooms/available` | Get only available rooms |
| `POST` | `/api/rooms` | Create a new room |
| `GET` | `/api/bookings` | Get all bookings |
| `GET` | `/api/bookings/{id}` | Get booking details |
| `POST` | `/api/bookings` | Create booking and add an in-app notification |
| `PUT` | `/api/bookings/{id}/cancel` | Cancel booking & release room bed |
| `GET` | `/api/bookings/student/{id}/active`| **HQL Query 2:** Get active booking for student |
| `POST` | `/api/admin/bookings/{id}/stay/check-in` | Record check-in |
| `POST` | `/api/admin/bookings/{id}/stay/check-out` | Record check-out (optional JSON `{"note":"..."}`) |

---

## 10. Git Commands & GitHub Setup

Initialize Git repository, commit files, and push to GitHub:
```powershell
# 1. Initialize repository
git init

# 2. Stage files
git add .

# 3. Commit initial project
git commit -m "feat: complete Student Accommodation Management System with microservices & AOP"

# 4. Link to GitHub repository
git remote add origin https://github.com/<your-username>/student-accommodation-system.git
git branch -M main

# 5. Push to GitHub
git push -u origin main
```
