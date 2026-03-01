package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findBySaleId(Long saleId);
    List<Payment> findByBookingId(Long bookingId);
    List<Payment> findByBookingCustomerEmailOrderByPaymentDateDesc(String email);
    List<Payment> findByStatus(String status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.paymentDate >= ?1 AND p.paymentDate <= ?2 AND p.status = 'Completed'")
    Double getTotalPaymentsBetweenDates(LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM Payment p WHERE p.status = 'Pending'")
    List<Payment> findPendingPayments();

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE DATE(p.paymentDate) = CURRENT_DATE AND p.status = 'Completed'")
    Double getTodayCashCollection();
}
