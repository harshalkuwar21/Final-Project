package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Notification;
import com.Dk3.Cars.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationsRestController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        try {
            List<Notification> list = notificationService.getAllNotifications();
            List<Map<String, Object>> out = new ArrayList<>();
            for (Notification n : list) out.add(entityToMap(n));
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Map<String, Object>>> getUnread() {
        try {
            List<Notification> list = notificationService.getUnreadNotifications();
            List<Map<String, Object>> out = new ArrayList<>();
            for (Notification n : list) out.add(entityToMap(n));
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }

    @GetMapping("/count-unread")
    public ResponseEntity<Map<String, Object>> countUnread() {
        Map<String, Object> resp = new HashMap<>();
        try {
            resp.put("count", notificationService.countUnread());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("count", 0);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @PostMapping("")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Notification notification) {
        Map<String, Object> resp = new HashMap<>();
        try {
            Notification saved = notificationService.saveNotification(notification);
            resp.put("success", true);
            resp.put("notification", entityToMap(saved));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @PostMapping("/mark-read/{id}")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            notificationService.markAsRead(id);
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllRead() {
        Map<String, Object> resp = new HashMap<>();
        try {
            notificationService.markAllAsRead();
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        try {
            notificationService.deleteNotification(id);
            resp.put("success", true);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    private Map<String, Object> entityToMap(Notification n) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", n.getId());
        m.put("title", n.getTitle());
        m.put("message", n.getMessage());
        m.put("type", n.getType());
        m.put("readFlag", n.isReadFlag());
        m.put("link", n.getLink());
        m.put("createdAt", n.getCreatedAt());
        return m;
    }
}
