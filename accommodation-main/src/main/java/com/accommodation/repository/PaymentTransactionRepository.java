package com.accommodation.repository;

import com.accommodation.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByHostelFeeIdOrderByPaymentDateDescCreatedAtDesc(Long feeId);

    List<PaymentTransaction> findByStudentIdOrderByPaymentDateDescCreatedAtDesc(Long studentId);

    boolean existsByTransactionReference(String transactionReference);

    Optional<PaymentTransaction> findByReceiptNumber(String receiptNumber);
}
