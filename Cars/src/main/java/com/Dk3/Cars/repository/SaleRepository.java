package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findTop5ByOrderBySoldDateDesc();

    // helper counters
    long countByStatus(String status);
    long countByStatusNot(String status);

    // bookings/sales by date
    long countBySoldDate(LocalDate date);

    // New methods for enhanced functionality
    List<Sale> findByCustomerId(Long customerId);
    List<Sale> findBySalesExecutiveUserid(Long salesExecutiveId);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.soldDate >= ?1 AND s.soldDate <= ?2")
    Double getTotalRevenueBetweenDates(LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE YEAR(s.soldDate) = ?1 AND MONTH(s.soldDate) = ?2")
    Double getMonthlyRevenue(int year, int month);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE YEAR(s.soldDate) = ?1")
    Double getYearlyRevenue(int year);

    @Query("SELECT s FROM Sale s WHERE s.status = 'Pending'")
    List<Sale> findPendingPayments();

    @Query("SELECT COUNT(s) FROM Sale s WHERE DATE(s.soldDate) = CURRENT_DATE")
    long countTodaySales();

    @Query("SELECT COUNT(s) FROM Sale s WHERE YEAR(s.soldDate) = YEAR(CURRENT_DATE) AND MONTH(s.soldDate) = MONTH(CURRENT_DATE)")
    long countCurrentMonthSales();

    @Query("SELECT COUNT(s) FROM Sale s WHERE YEAR(s.soldDate) = ?1")
    long countYearlySales(int year);
} 
