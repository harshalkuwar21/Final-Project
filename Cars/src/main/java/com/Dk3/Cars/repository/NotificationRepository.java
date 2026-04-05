package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReadFlagFalseOrderByCreatedAtDesc();
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);
    List<Notification> findByRecipientEmailAndReadFlagFalseOrderByCreatedAtDesc(String recipientEmail);
    Optional<Notification> findByIdAndRecipientEmail(Long id, String recipientEmail);

    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findAllOrderByCreatedAtDesc();
}
