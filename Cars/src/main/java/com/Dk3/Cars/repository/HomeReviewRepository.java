package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.HomeReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HomeReviewRepository extends JpaRepository<HomeReview, Long> {

    List<HomeReview> findTop6ByApprovedTrueOrderByCreatedAtDesc();

    List<HomeReview> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByApprovedTrue();

    @Query("SELECT AVG(r.rating) FROM HomeReview r WHERE r.approved = true")
    Double findAverageApprovedRating();
}
