package com.accommodation.config;

import com.accommodation.entity.*;
import com.accommodation.repository.*;
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
    private final HostelFeeRepository feeRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final InAppNotificationRepository notificationRepository;

    public DataInitializer(StudentRepository studentRepository,
                           RoomRepository roomRepository,
                           BookingRepository bookingRepository,
                           PersonRepository personRepository,
                           StudentPersonRepository studentPersonRepository,
                           HostelFeeRepository feeRepository,
                           PaymentTransactionRepository transactionRepository,
                           InAppNotificationRepository notificationRepository) {
        this.studentRepository = studentRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.personRepository = personRepository;
        this.studentPersonRepository = studentPersonRepository;
        this.feeRepository = feeRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        if (studentRepository.count() == 0 && roomRepository.count() == 0) {
            log.info("Populating initial sample data...");

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

            // 5. Seed Initial Hostel Fees Covering All Payment Status Scenarios
            LocalDate today = LocalDate.now();

            // Scenario 1: PAID IN FULL (Aarav Sharma - Monthly Fee)
            HostelFee f1 = new HostelFee(s1, "Kaveri Boys Hostel", "A-101", FeeType.MONTHLY_HOSTEL_FEE, 6500.0, today.minusDays(5));
            f1.setPaidAmount(6500.0);
            f1.setPaymentDate(today.minusDays(6));
            f1.setPaymentMethod("UPI");
            f1.setTransactionReference("UPI-202410-AARAV1");
            f1.recalculateStatus(today);
            feeRepository.save(f1);

            PaymentTransaction t1 = new PaymentTransaction(
                    f1, s1, 6500.0, today.minusDays(6), "UPI",
                    "UPI-202410-AARAV1", "REC-202410-1001", "Hostel Administrator", "Online UPI Payment"
            );
            transactionRepository.save(t1);

            InAppNotification n1 = new InAppNotification(
                    "STUDENT", s1, f1, NotificationCategory.PAYMENT_CONFIRMATION,
                    "Payment Received - Fully Paid",
                    "Dear Aarav Sharma, your payment of ₹6500.0 for Monthly Hostel Fee has been verified. Remaining balance: ₹0.00. Receipt: REC-202410-1001",
                    "/fees/receipts/REC-202410-1001", "INIT-AARAV-PAID"
            );
            notificationRepository.save(n1);

            // Scenario 2: PARTIALLY PAID (Priya Nair - Mess Fee, paid ₹2000 of ₹4500, balance ₹2500 due in 12 days)
            HostelFee f2 = new HostelFee(s2, "Yamuna Girls Hostel", "G-101", FeeType.MESS_FEE, 4500.0, today.plusDays(12));
            f2.setPaidAmount(2000.0);
            f2.setPaymentDate(today.minusDays(2));
            f2.setPaymentMethod("Net Banking");
            f2.setTransactionReference("NB-202410-PRIYA2");
            f2.recalculateStatus(today);
            feeRepository.save(f2);

            PaymentTransaction t2 = new PaymentTransaction(
                    f2, s2, 2000.0, today.minusDays(2), "Net Banking",
                    "NB-202410-PRIYA2", "REC-202410-1002", "Warden Office", "First Installment Paid"
            );
            transactionRepository.save(t2);

            InAppNotification n2 = new InAppNotification(
                    "STUDENT", s2, f2, NotificationCategory.PARTIAL_PAYMENT,
                    "Partial Payment Recorded",
                    "Dear Priya Nair, your partial payment of ₹2000.0 has been recorded. Remaining balance: ₹2500.0 due on " + f2.getDueDate(),
                    "/fees/receipts/REC-202410-1002", "INIT-PRIYA-PARTIAL"
            );
            notificationRepository.save(n2);

            // Scenario 3: OVERDUE (Rahul Verma - Monthly Fee, due 12 days ago, unpaid)
            HostelFee f3 = new HostelFee(s3, "Ganga Boys Hostel", "B-201", FeeType.MONTHLY_HOSTEL_FEE, 4500.0, today.minusDays(12));
            f3.recalculateStatus(today);
            feeRepository.save(f3);

            InAppNotification n3 = new InAppNotification(
                    "STUDENT", s3, f3, NotificationCategory.OVERDUE_FEE,
                    "OVERDUE FEE ALERT: 12 Days Overdue",
                    "URGENT: Dear Rahul Verma, your Monthly Hostel Fee of ₹4500.0 was due on " + f3.getDueDate() + " and is now 12 days overdue!",
                    "/student/fees", "INIT-RAHUL-OVERDUE"
            );
            InAppNotification n3Admin = new InAppNotification(
                    "ADMIN", null, f3, NotificationCategory.ADMINISTRATIVE_ALERT,
                    "Overdue Alert: Rahul Verma (12d overdue)",
                    "Student Rahul Verma has an outstanding overdue balance of ₹4500.0 for Ganga Boys Hostel, Room B-201.",
                    "/fees/" + f3.getId(), "INIT-ADMIN-RAHUL-OVERDUE"
            );
            notificationRepository.saveAll(List.of(n3, n3Admin));

            // Scenario 4: UPCOMING (Sneha Patel - Maintenance Fee, due in 4 days)
            HostelFee f4 = new HostelFee(s4, "Saraswati Girls Hostel", "G-201", FeeType.MAINTENANCE_FEE, 1200.0, today.plusDays(4));
            f4.recalculateStatus(today);
            feeRepository.save(f4);

            InAppNotification n4 = new InAppNotification(
                    "STUDENT", s4, f4, NotificationCategory.UPCOMING_FEE,
                    "Upcoming Fee Reminder: Due in 4 Days",
                    "Dear Sneha Patel, your Maintenance Fee of ₹1200.0 for Room G-201 is due in 4 days on " + f4.getDueDate() + ".",
                    "/student/fees", "INIT-SNEHA-UPCOMING"
            );
            notificationRepository.save(n4);

            // Scenario 5: PENDING (Kavya Reddy - Other Charges, due in 25 days)
            HostelFee f5 = new HostelFee(s5, "Yamuna Girls Hostel", "G-101", FeeType.OTHER_CHARGES, 1800.0, today.plusDays(25));
            f5.recalculateStatus(today);
            feeRepository.save(f5);

            log.info("Sample data initialization complete! Student accommodation, hostel fees, and alert records ready.");
        }
    }
}
