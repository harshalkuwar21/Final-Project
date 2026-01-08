package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;
public interface CarRepository extends JpaRepository<Car, Long> {

    long countBySold(boolean sold);

    @Query("select sum(c.price) from Car c where c.sold = true")
    Double totalRevenue();

    // ✅ Inventory Overview (Brand wise count)
    @Query("SELECT c.brand, COUNT(c) FROM Car c GROUP BY c.brand")
    List<Object[]> countCarsByBrand();

    // ✅ Top Selling Cars
    @Query("""
    SELECT c.model, COUNT(s.id)
    FROM Sale s 
    JOIN s.car c
    GROUP BY c.model
    ORDER BY COUNT(s.id) DESC
""")
    List<Object[]> topSellingCars(Pageable pageable);
    List<Car> findByShowroomId(Long showroomId);


}
