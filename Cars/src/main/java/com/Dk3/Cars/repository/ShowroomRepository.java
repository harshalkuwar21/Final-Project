package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Showroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShowroomRepository extends JpaRepository<Showroom, Long> {
    @Query("""
        SELECT s.id, COUNT(c.id)
        FROM Showroom s
        LEFT JOIN Car c ON c.showroom.id = s.id AND c.status = 'Available'
        GROUP BY s.id
        """)
    List<Object[]> getAvailableCarCountByShowroom();
}

