package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.LoanDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanDetailRepository extends JpaRepository<LoanDetail, Long> {
    Optional<LoanDetail> findByBookingId(Long bookingId);
}
