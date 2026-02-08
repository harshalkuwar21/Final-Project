package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByCarId(Long carId);
    List<Document> findByCustomerId(Long customerId);
    List<Document> findByDocumentType(String documentType);

    @Query("SELECT d FROM Document d WHERE d.expiryDate <= ?1 AND d.expiryDate IS NOT NULL")
    List<Document> findExpiringDocuments(LocalDate date);

    @Query("SELECT d FROM Document d WHERE d.expiryDate BETWEEN CURRENT_DATE AND ?1 AND d.expiryDate IS NOT NULL")
    List<Document> findDocumentsExpiringSoon(LocalDate futureDate);
}