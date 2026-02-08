package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Notification;
import com.Dk3.Cars.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllOrderByCreatedAtDesc();
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadFlagFalseOrderByCreatedAtDesc();
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public long countUnread() {
        return notificationRepository.findByReadFlagFalseOrderByCreatedAtDesc().size();
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setReadFlag(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead() {
        notificationRepository.findByReadFlagFalseOrderByCreatedAtDesc().forEach(n -> {
            n.setReadFlag(true);
        });
        // Save changes in bulk
        notificationRepository.saveAll(notificationRepository.findAll());
    }
}
