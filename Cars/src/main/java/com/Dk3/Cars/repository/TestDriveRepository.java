package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.TestDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface TestDriveRepository extends JpaRepository<TestDrive, Long> {

    List<TestDrive> findByStatus(String status);
    List<TestDrive> findByCustomerId(Long customerId);
    List<TestDrive> findBySalesExecutiveUserid(Long salesExecutiveId);

    @Query("SELECT t FROM TestDrive t WHERE t.scheduledDateTime >= ?1 AND t.scheduledDateTime <= ?2")
    List<TestDrive> findScheduledTestDrivesBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(t) FROM TestDrive t WHERE t.status = 'Scheduled' AND DATE(t.scheduledDateTime) = CURRENT_DATE")
    long countTodayScheduledTestDrives();

    @Query("SELECT COUNT(t) FROM TestDrive t WHERE t.convertedToSale = true")
    long countConvertedToSales();

    @Query("SELECT t FROM TestDrive t WHERE t.status = 'Completed' AND t.feedback IS NOT NULL")
    List<TestDrive> findCompletedWithFeedback();
}