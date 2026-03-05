package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.BankDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankDetailRepository extends JpaRepository<BankDetail, Long> {
    List<BankDetail> findByActiveTrueOrderByBankNameAsc();
}
