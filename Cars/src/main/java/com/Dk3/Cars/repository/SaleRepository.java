package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findTop5ByOrderBySoldDateDesc();
}
