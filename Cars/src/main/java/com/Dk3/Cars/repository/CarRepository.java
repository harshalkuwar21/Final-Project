package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {

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
    List<Car> findByShowroomIdOrderByIdDesc(Long showroomId);

    long countBySold(boolean sold);

    // New methods for enhanced functionality
    List<Car> findByBrand(String brand);
    List<Car> findByFuelType(String fuelType);
    List<Car> findByStatus(String status);
    List<Car> findByBrandAndModel(String brand, String model);

    @Query("SELECT c FROM Car c WHERE c.status = 'Available'")
    List<Car> findAvailableCars();

    @Query("SELECT c FROM Car c WHERE c.stockQuantity <= 5 AND c.status = 'Available'")
    List<Car> findLowStockCars();

    @Query("SELECT c.brand, c.model, COUNT(c) FROM Car c WHERE c.stockQuantity <= 5 GROUP BY c.brand, c.model")
    List<Object[]> findLowStockModels();

    @Query("SELECT COUNT(c) FROM Car c WHERE c.status = 'Available'")
    long countAvailableCars();

    @Query("SELECT c FROM Car c WHERE LOWER(c.brand) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(c.model) LIKE LOWER(CONCAT('%', ?1, '%')) OR LOWER(c.fuelType) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Car> searchCars(String keyword);
}
