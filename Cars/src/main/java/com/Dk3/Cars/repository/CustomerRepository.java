package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByLeadSource(String leadSource);

    @Query("SELECT c FROM Customer c WHERE c.nextFollowUpDate <= CURRENT_DATE AND c.nextFollowUpDate IS NOT NULL")
    List<Customer> findCustomersNeedingFollowUp();

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.leadSource = ?1")
    long countByLeadSource(String leadSource);
}