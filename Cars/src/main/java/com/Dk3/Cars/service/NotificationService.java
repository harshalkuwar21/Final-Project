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

    public List<Notification> getNotificationsForRecipient(String recipientEmail) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(recipientEmail);
    }

    public List<Notification> getUnreadNotificationsForRecipient(String recipientEmail) {
        return notificationRepository.findByRecipientEmailAndReadFlagFalseOrderByCreatedAtDesc(recipientEmail);
    }

    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Notification createUserNotification(String recipientEmail, String title, String message, String type, String link) {
        Notification notification = new Notification();
        notification.setRecipientEmail(recipientEmail);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type == null || type.isBlank() ? "INFO" : type);
        notification.setLink(link);
        notification.setReadFlag(false);
        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    public long countUnread() {
        return notificationRepository.findByReadFlagFalseOrderByCreatedAtDesc().size();
    }

    public long countUnreadForRecipient(String recipientEmail) {
        return notificationRepository.findByRecipientEmailAndReadFlagFalseOrderByCreatedAtDesc(recipientEmail).size();
    }

    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setReadFlag(true);
            notificationRepository.save(n);
        });
    }

    public void markAsReadForRecipient(Long id, String recipientEmail) {
        notificationRepository.findByIdAndRecipientEmail(id, recipientEmail).ifPresent(n -> {
            n.setReadFlag(true);
            notificationRepository.save(n);
        });
    }

    public void markAllAsRead() {
        List<Notification> unread = notificationRepository.findByReadFlagFalseOrderByCreatedAtDesc();
        unread.forEach(n -> n.setReadFlag(true));
        notificationRepository.saveAll(unread);
    }

    public void markAllAsReadForRecipient(String recipientEmail) {
        List<Notification> unread = notificationRepository.findByRecipientEmailAndReadFlagFalseOrderByCreatedAtDesc(recipientEmail);
        unread.forEach(n -> n.setReadFlag(true));
        notificationRepository.saveAll(unread);
    }
}
