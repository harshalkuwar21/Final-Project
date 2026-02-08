package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.ServiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {

    List<ServiceRecord> findByCarId(Long carId);

    @Query("SELECT s FROM ServiceRecord s WHERE s.nextServiceDate <= ?1 AND s.nextServiceDate IS NOT NULL")
    List<ServiceRecord> findUpcomingServices(LocalDate date);

    @Query("SELECT s FROM ServiceRecord s WHERE s.warrantyExpiryDate <= ?1 AND s.warrantyExpiryDate IS NOT NULL")
    List<ServiceRecord> findExpiringWarranties(LocalDate date);

    @Query("SELECT SUM(s.cost) FROM ServiceRecord s WHERE s.serviceDate >= ?1 AND s.serviceDate <= ?2")
    Double getServiceCostsBetweenDates(LocalDate startDate, LocalDate endDate);
}