package com.accommodation.service;

import com.accommodation.dto.BookingRequestDTO;
import com.accommodation.entity.Booking;
import com.accommodation.entity.NotificationCategory;
import com.accommodation.entity.Room;
import com.accommodation.entity.Student;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.exception.ResourceNotFoundException;
import com.accommodation.exception.RoomNotAvailableException;
import com.accommodation.repository.BookingRepository;
import com.accommodation.repository.RoomRepository;
import com.accommodation.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final AccommodationStayService accommodationStayService;
    private final InAppNotificationService inAppNotificationService;

    public BookingService(BookingRepository bookingRepository,
                          StudentRepository studentRepository,
                          RoomRepository roomRepository,
                          AccommodationStayService accommodationStayService,
                          InAppNotificationService inAppNotificationService) {
        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.accommodationStayService = accommodationStayService;
        this.inAppNotificationService = inAppNotificationService;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));
    }

    public List<Booking> getBookingsByStatus(String status) {
        if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("ALL")) {
            return getAllBookings();
        }
        return bookingRepository.findByStatus(status.trim().toUpperCase());
    }

    /**
     * Creates a new booking within a database transaction.
     * Enforces business rules:
     * 1. Student must exist.
     * 2. Student must NOT already hold an ACTIVE booking.
     * 3. Room must exist.
     * 4. Room must have available beds.
     * 5. Atomically increments room.occupiedBeds and updates room availability.
     * 6. Persists an in-app booking notification in the same transaction.
     */
    @Transactional
    public Booking createBooking(BookingRequestDTO request) {
        Student student = studentRepository.findByIdForUpdate(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + request.getStudentId()));

        Room room = roomRepository.findByIdForUpdate(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.getRoomId()));

        // Prevent duplicate active booking for the same student
        if (bookingRepository.existsByStudentIdAndStatus(student.getId(), "ACTIVE")) {
            throw new DuplicateResourceException("Student " + student.getFullName() + 
                    " (" + student.getRegisterNumber() + ") already has an ACTIVE accommodation booking!");
        }

        // Enforce Gender-based Hostel Allocation Rule
        String studentGender = student.getGender() != null ? student.getGender().trim().toUpperCase() : "MALE";
        String hostelType = room.getHostelType() != null ? room.getHostelType().trim().toUpperCase() : "BOYS";

        if (!hostelType.equalsIgnoreCase("COED")) {
            boolean isMaleInBoys = "MALE".equalsIgnoreCase(studentGender) && "BOYS".equalsIgnoreCase(hostelType);
            boolean isFemaleInGirls = "FEMALE".equalsIgnoreCase(studentGender) && "GIRLS".equalsIgnoreCase(hostelType);

            if (!isMaleInBoys && !isFemaleInGirls) {
                String expected = "MALE".equalsIgnoreCase(studentGender) ? "Boys Hostel" : "Girls Hostel";
                String actual = "BOYS".equalsIgnoreCase(hostelType) ? "Boys Hostel" : "Girls Hostel";
                throw new IllegalArgumentException("Hostel Gender Restriction: " + student.getFullName() + 
                        " is a " + studentGender + " student and cannot be allocated to " + actual + " (" + 
                        room.getBuildingName() + " - Room " + room.getRoomNumber() + ")! Please choose a " + expected + ".");
            }
        }

        // Validate available beds
        if (room.getAvailableBeds() <= 0) {
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() + 
                    " has no available beds! Capacity: " + room.getCapacity() + ", Occupied: " + room.getOccupiedBeds());
        }

        // Update room occupancy
        room.setOccupiedBeds(room.getOccupiedBeds() + 1);
        room.updateAvailability();
        roomRepository.save(room);

        // Create booking
        LocalDate bookingDate = request.getBookingDate() != null ? request.getBookingDate() : LocalDate.now();
        Booking booking = new Booking(student, room, bookingDate, "ACTIVE");
        Booking savedBooking = bookingRepository.save(booking);
        accommodationStayService.initialize(savedBooking);

        inAppNotificationService.createNotification(
                "STUDENT", student, null, NotificationCategory.BOOKING_UPDATE,
                "Accommodation booking confirmed",
                "Room " + room.getRoomNumber() + " in " + room.getBuildingName() + " is assigned to you.",
                "/notifications", "booking-created:" + savedBooking.getId());

        return savedBooking;
    }

    /**
     * Cancels an existing active booking within a database transaction.
     * Atomically decrements room.occupiedBeds and updates room availability status.
     */
    @Transactional
    public Booking cancelBooking(Long id) {
        Booking booking = bookingRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + id));

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Booking ID " + id + " is already CANCELLED.");
        }

        booking.setStatus("CANCELLED");
        Booking updatedBooking = bookingRepository.save(booking);
        inAppNotificationService.createNotification(
                "STUDENT", booking.getStudent(), null, NotificationCategory.BOOKING_UPDATE,
                "Accommodation booking cancelled",
                "Your booking for room " + booking.getRoom().getRoomNumber() + " has been cancelled.",
                "/notifications", "booking-cancelled:" + booking.getId());

        Room room = booking.getRoom();
        if (room != null && room.getOccupiedBeds() > 0) {
            room.setOccupiedBeds(room.getOccupiedBeds() - 1);
            room.updateAvailability();
            roomRepository.save(room);
        }

        return updatedBooking;
    }

    public long countTotalBookings() {
        return bookingRepository.count();
    }

    /**
     * Demonstrates HQL Query 2: Find active bookings for a particular student
     */
    public List<Booking> getActiveBookingsByStudentHql(Long studentId) {
        return bookingRepository.findActiveBookingsByStudentHql(studentId);
    }
}
