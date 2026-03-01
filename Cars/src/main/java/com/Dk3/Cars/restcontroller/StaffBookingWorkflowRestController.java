package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.BookingRepository;
import com.Dk3.Cars.repository.UserRepository;
import com.Dk3.Cars.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/staff/bookings")
public class StaffBookingWorkflowRestController {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserRepository userRepository;

    private boolean isStaffOrAdmin(HttpSession session) {
        String role = String.valueOf(session.getAttribute("USER_ROLE"));
        return "ROLE_ADMIN".equals(role) || (role != null && !"ROLE_USER".equals(role) && !"null".equals(role));
    }

    private Optional<User> getSessionUser(HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) return Optional.empty();
        Long userId = Long.valueOf(String.valueOf(userIdObj));
        return userRepository.findById(userId);
    }

    private boolean isAdmin(HttpSession session) {
        return "ROLE_ADMIN".equals(String.valueOf(session.getAttribute("USER_ROLE")));
    }

    private boolean canAccessBooking(HttpSession session, Booking booking) {
        if (isAdmin(session)) return true;
        Long staffShowroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
        Long bookingShowroomId = booking != null && booking.getCar() != null && booking.getCar().getShowroom() != null
                ? booking.getCar().getShowroom().getId() : null;
        return staffShowroomId != null && staffShowroomId.equals(bookingShowroomId);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> pending(HttpSession session) {
        if (!isStaffOrAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }
        List<Booking> bookings;
        if (isAdmin(session)) {
            bookings = bookingRepository.findByWorkflowStatus("Pending");
        } else {
            Long showroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
            if (showroomId == null) {
                bookings = java.util.Collections.emptyList();
            } else {
                bookings = bookingRepository.findByWorkflowStatusAndCarShowroomId("Pending", showroomId);
            }
        }
        return ResponseEntity.ok(Map.of("ok", true, "bookings", bookings));
    }

    @PostMapping("/{id}/verify-payment")
    public ResponseEntity<?> verifyPayment(@PathVariable Long id, HttpSession session) {
        if (!isStaffOrAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }
        Optional<Booking> existing = bookingRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        if (!canAccessBooking(session, existing.get())) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        }
        Optional<Booking> updated = bookingService.updateWorkflowStatus(id, "Payment Verified", null, null);
        if (updated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        return ResponseEntity.ok(Map.of("ok", true, "booking", updated.get()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate confirmedDeliveryDate,
            HttpSession session) {
        if (!isStaffOrAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }
        Optional<Booking> existing = bookingRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        if (!canAccessBooking(session, existing.get())) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        }
        Optional<Booking> updated = bookingService.updateWorkflowStatus(id, "Approved", null, confirmedDeliveryDate);
        if (updated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        return ResponseEntity.ok(Map.of("ok", true, "booking", updated.get()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestParam String reason, HttpSession session) {
        if (!isStaffOrAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }
        Optional<Booking> existing = bookingRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        if (!canAccessBooking(session, existing.get())) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        }
        Optional<Booking> updated = bookingService.updateWorkflowStatus(id, "Rejected", reason, null);
        if (updated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        return ResponseEntity.ok(Map.of("ok", true, "booking", updated.get()));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        if (!isStaffOrAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
        }
        Optional<Booking> existing = bookingRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        if (!canAccessBooking(session, existing.get())) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        }
        Optional<Booking> updated = bookingService.updateWorkflowStatus(id, status, null, null);
        if (updated.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        return ResponseEntity.ok(Map.of("ok", true, "booking", updated.get()));
    }
}
