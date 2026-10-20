# REST API Specification & Postman Testing Guide

Base URL: `http://localhost:8080` (Main Service)  
Notification Service Base URL: `http://localhost:8081`

---

## 1. Student REST APIs

### 1.1 Get All Students
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/students`
- **Headers:** `Accept: application/json`
- **Response (200 OK):**
```json
[
  {
    "id": 1,
    "fullName": "Aarav Sharma",
    "registerNumber": "2024CS101",
    "email": "aarav.sharma@college.edu",
    "phoneNumber": "+91 9876543210",
    "department": "Computer Science",
    "yearOfStudy": 2
  }
]
```

### 1.2 Get Student by ID
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/students/1`
- **Response (200 OK):**
```json
{
  "id": 1,
  "fullName": "Aarav Sharma",
  "registerNumber": "2024CS101",
  "email": "aarav.sharma@college.edu",
  "phoneNumber": "+91 9876543210",
  "department": "Computer Science",
  "yearOfStudy": 2
}
```
- **Error Response (404 Not Found):**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Student not found with ID: 999",
  "timestamp": "2026-10-19T15:20:00"
}
```

### 1.3 Create a Student
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/students`
- **Headers:** `Content-Type: application/json`
- **Request Body:**
```json
{
  "fullName": "Meera Krishnan",
  "registerNumber": "2024EE505",
  "email": "meera.k@college.edu",
  "phoneNumber": "+91 9876543220",
  "department": "Electrical Engineering",
  "yearOfStudy": 1
}
```
- **Response (201 Created):**
```json
{
  "id": 6,
  "fullName": "Meera Krishnan",
  "registerNumber": "2024EE505",
  "email": "meera.k@college.edu",
  "phoneNumber": "+91 9876543220",
  "department": "Electrical Engineering",
  "yearOfStudy": 1
}
```
- **Validation Error Response (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "email": "Please provide a valid email address",
    "fullName": "Full name is required"
  },
  "timestamp": "2026-10-19T15:20:00"
}
```

### 1.4 HQL Query: Find Students by Department
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/students/department/Computer Science`
- **Response (200 OK):** List of matching students in the given department.

---

## 2. Room REST APIs

### 2.1 Get All Rooms
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/rooms`
- **Response (200 OK):**
```json
[
  {
    "id": 1,
    "roomNumber": "A-101",
    "buildingName": "Kaveri Hostel",
    "roomType": "Single",
    "capacity": 1,
    "occupiedBeds": 1,
    "monthlyRent": 6500.0,
    "status": "FULL",
    "availableBeds": 0
  }
]
```

### 2.2 Get Available Rooms Only
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/rooms/available`
- **Response (200 OK):** Array of rooms where `status = "AVAILABLE"` and `availableBeds > 0`.

### 2.3 Create a Room
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/rooms`
- **Request Body:**
```json
{
  "roomNumber": "D-401",
  "buildingName": "Yamuna Hostel",
  "roomType": "Double Sharing",
  "capacity": 2,
  "occupiedBeds": 0,
  "monthlyRent": 4800.0
}
```

---

## 3. Booking REST APIs

### 3.1 Get All Bookings
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/bookings`
- **Response (200 OK):**
```json
[
  {
    "bookingId": 1,
    "bookingDate": "2026-10-04",
    "status": "ACTIVE",
    "student": {
      "id": 1,
      "fullName": "Aarav Sharma",
      "registerNumber": "2024CS101",
      "email": "aarav.sharma@college.edu",
      "department": "Computer Science"
    },
    "room": {
      "id": 1,
      "roomNumber": "A-101",
      "buildingName": "Kaveri Hostel",
      "roomType": "Single",
      "monthlyRent": 6500.0
    }
  }
]
```

### 3.2 Create a Booking (Allocate Bed)
- **Method:** `POST`
- **URL:** `http://localhost:8080/api/bookings`
- **Headers:** `Content-Type: application/json`
- **Request Body:**
```json
{
  "studentId": 3,
  "roomId": 3,
  "bookingDate": "2026-10-19"
}
```
- **Response (201 Created):**
```json
{
  "bookingId": 3,
  "bookingDate": "2026-10-19",
  "status": "ACTIVE",
  "student": {
    "id": 3,
    "fullName": "Rahul Verma",
    "registerNumber": "2024ME303"
  },
  "room": {
    "id": 3,
    "roomNumber": "B-202"
  }
}
```
- **Conflict Error (Duplicate Active Booking) (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request / Conflict",
  "message": "Student Rahul Verma (2024ME303) already has an ACTIVE accommodation booking!",
  "timestamp": "2026-10-19T15:20:00"
}
```

### 3.3 Cancel a Booking
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/bookings/3/cancel`
- **Response (200 OK):**
```json
{
  "bookingId": 3,
  "status": "CANCELLED",
  "message": "Booking cancelled successfully and room bed released."
}
```

---

## 4. Notification Microservice API (Port 8081)

- **Method:** `POST`
- **URL:** `http://localhost:8081/api/notifications/booking`
- **Request Body:**
```json
{
  "studentName": "Aarav Sharma",
  "studentEmail": "aarav.sharma@college.edu",
  "roomNumber": "A-101",
  "bookingStatus": "ACTIVE"
}
```
- **Response (200 OK):**
```json
{
  "status": "SUCCESS",
  "message": "Simulated booking notification logged successfully by Notification Service",
  "studentEmail": "aarav.sharma@college.edu",
  "roomNumber": "A-101"
}
```

---

## 5. Postman Testing Workflow

1. **Importing / Setup:**
   - In Postman, create a collection named `Student Accommodation System`.
   - Add variable `baseUrl` = `http://localhost:8080`.
2. **Execute in Sequence:**
   1. `GET {{baseUrl}}/api/students` -> Verify seeded sample students.
   2. `POST {{baseUrl}}/api/students` -> Add a new student and copy their `id`.
   3. `GET {{baseUrl}}/api/rooms/available` -> Check available room IDs.
   4. `POST {{baseUrl}}/api/bookings` -> Create a booking with the new `studentId` and available `roomId`.
   5. Verify that room occupancy increments automatically.
   6. Check Notification Service console for the simulated alert output.
   7. `PUT {{baseUrl}}/api/bookings/{id}/cancel` -> Cancel booking and verify bed is released.
