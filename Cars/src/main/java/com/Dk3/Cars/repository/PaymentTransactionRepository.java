package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
