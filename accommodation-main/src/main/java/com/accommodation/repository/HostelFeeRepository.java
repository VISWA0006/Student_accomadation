package com.accommodation.repository;

import com.accommodation.entity.HostelFee;
import com.accommodation.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HostelFeeRepository extends JpaRepository<HostelFee, Long> {

    List<HostelFee> findByStudentIdOrderByDueDateDesc(Long studentId);

    List<HostelFee> findByStatus(PaymentStatus status);

    List<HostelFee> findByStatusOrderByDueDateAsc(PaymentStatus status);

    @Query("SELECT f FROM HostelFee f WHERE f.remainingBalance > 0 AND f.dueDate < :currentDate ORDER BY f.dueDate ASC")
    List<HostelFee> findOverdueFees(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT f FROM HostelFee f WHERE f.remainingBalance > 0 AND f.dueDate >= :currentDate AND f.dueDate <= :endDate ORDER BY f.dueDate ASC")
    List<HostelFee> findUpcomingFees(@Param("currentDate") LocalDate currentDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT f FROM HostelFee f WHERE " +
           "(:hostelName IS NULL OR LOWER(f.hostelName) LIKE LOWER(CONCAT('%', :hostelName, '%'))) AND " +
           "(:roomNumber IS NULL OR LOWER(f.roomNumber) LIKE LOWER(CONCAT('%', :roomNumber, '%'))) AND " +
           "(:search IS NULL OR LOWER(f.student.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(f.student.registerNumber) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR f.status = :status) " +
           "ORDER BY f.dueDate DESC")
    List<HostelFee> filterFees(@Param("hostelName") String hostelName,
                               @Param("roomNumber") String roomNumber,
                               @Param("search") String search,
                               @Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(f.totalAmount), 0.0) FROM HostelFee f")
    Double getTotalFeesDue();

    @Query("SELECT COALESCE(SUM(f.paidAmount), 0.0) FROM HostelFee f")
    Double getTotalAmountCollected();

    @Query("SELECT COALESCE(SUM(f.remainingBalance), 0.0) FROM HostelFee f")
    Double getTotalOutstandingBalance();

    @Query("SELECT COUNT(DISTINCT f.student.id) FROM HostelFee f WHERE f.status = 'OVERDUE'")
    Long countDistinctOverdueStudents();

    @Query("SELECT COUNT(DISTINCT f.student.id) FROM HostelFee f WHERE f.status = 'PARTIALLY_PAID'")
    Long countDistinctPartiallyPaidStudents();

    @Query("SELECT COUNT(f) FROM HostelFee f WHERE f.status = 'UPCOMING'")
    Long countUpcomingPayments();
}
