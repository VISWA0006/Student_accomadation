package com.accommodation.service;

import com.accommodation.dto.BookingRequestDTO;
import com.accommodation.entity.Booking;
import com.accommodation.entity.Room;
import com.accommodation.entity.Student;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.exception.RoomNotAvailableException;
import com.accommodation.repository.BookingRepository;
import com.accommodation.repository.RoomRepository;
import com.accommodation.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private AccommodationStayService accommodationStayService;

    @Mock
    private InAppNotificationService inAppNotificationService;

    @InjectMocks
    private BookingService bookingService;

    private Student student;
    private Room room;

    @BeforeEach
    void setUp() {
        student = new Student("Priya Nair", "2024EC202", "priya@college.edu", "+91 9876543211", "Electronics", 3, "FEMALE");
        student.setId(10L);

        room = new Room("G-101", "Yamuna Girls Hostel", "Double Sharing", 2, 0, 4500.0, "GIRLS");
        room.setId(20L);
    }

    @Test
    @DisplayName("Should create booking and increment room occupied beds")
    void testCreateBookingSuccess() {
        BookingRequestDTO request = new BookingRequestDTO(10L, 20L, LocalDate.now());

        when(studentRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(student));
        when(roomRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByStudentIdAndStatus(10L, "ACTIVE")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = bookingService.createBooking(request);

        assertNotNull(booking);
        assertEquals("ACTIVE", booking.getStatus());
        assertEquals(1, room.getOccupiedBeds()); // Incremented from 0 to 1
        verify(roomRepository, times(1)).save(room);
        verify(inAppNotificationService, times(1)).createNotification(
                eq("STUDENT"), eq(student), isNull(), eq(com.accommodation.entity.NotificationCategory.BOOKING_UPDATE),
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if female student is allocated to boys hostel")
    void testCreateBookingGenderMismatchThrowsException() {
        Room boysRoom = new Room("B-101", "Kaveri Boys Hostel", "Single", 1, 0, 5000.0, "BOYS");
        boysRoom.setId(30L);
        BookingRequestDTO request = new BookingRequestDTO(10L, 30L, LocalDate.now());

        when(studentRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(student)); // Priya (FEMALE)
        when(roomRepository.findByIdForUpdate(30L)).thenReturn(Optional.of(boysRoom));  // Boys Hostel
        when(bookingRepository.existsByStudentIdAndStatus(10L, "ACTIVE")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(request));
        assertTrue(ex.getMessage().contains("Hostel Gender Restriction"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException if student already has active booking")
    void testCreateBookingDuplicateActiveThrowsException() {
        BookingRequestDTO request = new BookingRequestDTO(10L, 20L, LocalDate.now());

        when(studentRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(student));
        when(roomRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByStudentIdAndStatus(10L, "ACTIVE")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should throw RoomNotAvailableException if room is full")
    void testCreateBookingRoomFullThrowsException() {
        room.setOccupiedBeds(2); // capacity is 2 -> available beds = 0
        BookingRequestDTO request = new BookingRequestDTO(10L, 20L, LocalDate.now());

        when(studentRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(student));
        when(roomRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByStudentIdAndStatus(10L, "ACTIVE")).thenReturn(false);

        assertThrows(RoomNotAvailableException.class, () -> bookingService.createBooking(request));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should cancel booking and decrement occupied beds")
    void testCancelBookingSuccess() {
        room.setOccupiedBeds(1);
        Booking booking = new Booking(student, room, LocalDate.now(), "ACTIVE");
        booking.setId(100L);

        when(bookingRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking cancelled = bookingService.cancelBooking(100L);

        assertEquals("CANCELLED", cancelled.getStatus());
        assertEquals(0, room.getOccupiedBeds()); // Decremented from 1 to 0
        verify(roomRepository, times(1)).save(room);
    }
}
