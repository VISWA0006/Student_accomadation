package com.accommodation.config;

import com.accommodation.entity.Booking;
import com.accommodation.entity.Person;
import com.accommodation.entity.Room;
import com.accommodation.entity.Student;
import com.accommodation.entity.StudentPerson;
import com.accommodation.repository.BookingRepository;
import com.accommodation.repository.PersonRepository;
import com.accommodation.repository.RoomRepository;
import com.accommodation.repository.StudentPersonRepository;
import com.accommodation.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

/**
 * Initializes sample data on application startup if database is empty.
 * Ensures the system is ready for demonstration right after launching.
 */
@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PersonRepository personRepository;
    private final StudentPersonRepository studentPersonRepository;

    public DataInitializer(StudentRepository studentRepository,
                           RoomRepository roomRepository,
                           BookingRepository bookingRepository,
                           PersonRepository personRepository,
                           StudentPersonRepository studentPersonRepository) {
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.personRepository = personRepository;
        this.studentPersonRepository = studentPersonRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() == 0 && roomRepository.count() == 0) {
            log.info("Populating initial sample data for college demonstration...");

            // 1. Create Sample Students with Gender
            Student s1 = new Student("Aarav Sharma", "2024CS101", "aarav.sharma@college.edu", "+91 9876543210", "Computer Science", 2, "MALE");
            Student s2 = new Student("Priya Nair", "2024EC202", "priya.nair@college.edu", "+91 9876543211", "Electronics", 3, "FEMALE");
            Student s3 = new Student("Rahul Verma", "2024ME303", "rahul.verma@college.edu", "+91 9876543212", "Mechanical", 1, "MALE");
            Student s4 = new Student("Sneha Patel", "2024IT404", "sneha.patel@college.edu", "+91 9876543213", "Information Tech", 4, "FEMALE");
            Student s5 = new Student("Kavya Reddy", "2024CS105", "kavya.reddy@college.edu", "+91 9876543214", "Computer Science", 2, "FEMALE");

            studentRepository.saveAll(List.of(s1, s2, s3, s4, s5));

            // 2. Create Sample Rooms with Split Hostel Types (BOYS / GIRLS)
            Room r1 = new Room("A-101", "Kaveri Boys Hostel", "Single", 1, 1, 6500.0, "BOYS");
            Room r2 = new Room("B-201", "Ganga Boys Hostel", "Double Sharing", 2, 0, 4500.0, "BOYS");
            Room r3 = new Room("G-101", "Yamuna Girls Hostel", "Double Sharing", 2, 1, 4800.0, "GIRLS");
            Room r4 = new Room("G-201", "Saraswati Girls Hostel", "Triple Sharing", 3, 0, 3500.0, "GIRLS");

            roomRepository.saveAll(List.of(r1, r2, r3, r4));

            // 3. Create Sample Bookings (Aarav in Boys Hostel, Priya in Girls Hostel)
            Booking b1 = new Booking(s1, r1, LocalDate.now().minusDays(15), "ACTIVE");
            Booking b2 = new Booking(s2, r3, LocalDate.now().minusDays(5), "ACTIVE");

            bookingRepository.saveAll(List.of(b1, b2));

            // 4. Create Inheritance Mapping Sample Records (Person & StudentPerson)
            Person p1 = new Person("Dr. S. Ramanathan", "warden@college.edu");
            personRepository.save(p1);

            StudentPerson sp1 = new StudentPerson("Vikram Sundar", "vikram.s@college.edu", "2024SP901", "M.Tech Software Eng");
            studentPersonRepository.save(sp1);

            log.info("Sample data initialization complete! Ready for demonstration.");
        }
    }
}
