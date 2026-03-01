package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStatus(String status);
    List<Booking> findByWorkflowStatus(String workflowStatus);
    List<Booking> findByCarShowroomIdOrderByBookingDateDesc(Long showroomId);
    List<Booking> findByWorkflowStatusAndCarShowroomId(String workflowStatus, Long showroomId);
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByCustomerEmailOrderByBookingDateDesc(String email);
    List<Booking> findByCustomerIdOrderByBookingDateDesc(Long customerId);
    List<Booking> findBySalesExecutiveUserid(Long salesExecutiveId);

    @Query("SELECT b FROM Booking b WHERE b.expectedDeliveryDate <= ?1 AND b.status = 'Confirmed'")
    List<Booking> findPendingDeliveries(LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = ?1")
    long countByStatus(String status);

    @Query("SELECT b FROM Booking b ORDER BY b.bookingDate DESC")
    List<Booking> findRecentBookings();
}
