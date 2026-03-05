package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.PaymentStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentStageRepository extends JpaRepository<PaymentStage, Long> {
    List<PaymentStage> findByBookingIdOrderByStageOrderNoAsc(Long bookingId);
}
