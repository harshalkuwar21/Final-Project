package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.Customer;
import com.Dk3.Cars.entity.Payment;
import com.Dk3.Cars.repository.BookingRepository;
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/staff-dashboard")
public class StaffDashboardRestController {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping
    public Map<String, Object> getDashboard() {
        Map<String, Object> out = new HashMap<>();

        long totalCars = carRepository.count();
        long totalBookings = bookingRepository.count();

        long confirmed = countByStatuses("CONFIRMED", "Confirmed");
        long rejected = countByStatuses("REJECTED", "Rejected");
        long waiting = countByStatuses("WAITING", "Pending", "PendingApproval");

        double pendingPayments = sumPaymentsByStatuses("Pending", "PENDING");
        double completedPayments = sumPaymentsByStatuses("Completed", "COMPLETED", "Paid", "PAID");
        double totalSales = pendingPayments + completedPayments;

        List<Booking> bookings = bookingRepository.findRecentBookings();
        List<Map<String, Object>> bookingDtos = new ArrayList<>();
        for (Booking b : bookings) {
            bookingDtos.add(mapBooking(b));
        }

        out.put("totalCars", totalCars);
        out.put("totalBookings", totalBookings);
        out.put("confirmed", confirmed);
        out.put("rejected", rejected);
        out.put("waiting", waiting);
        out.put("totalSales", totalSales);
        out.put("pendingPayments", pendingPayments);
        out.put("completedPayments", completedPayments);
        out.put("bookings", bookingDtos);

        return out;
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Booking b = bookingRepository.findById(id).orElse(null);
        if (b == null) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "message", "Booking not found"));
        }
        b.setStatus(status);
        bookingRepository.save(b);
        return ResponseEntity.ok(Map.of("ok", true, "id", b.getId(), "status", b.getStatus()));
    }

    @PutMapping("/schedule/{id}")
    public ResponseEntity<?> scheduleDelivery(@PathVariable Long id, @RequestParam String date) {
        Booking b = bookingRepository.findById(id).orElse(null);
        if (b == null) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "message", "Booking not found"));
        }
        if (date == null || date.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Delivery date is required"));
        }
        LocalDate deliveryDate = LocalDate.parse(date);
        b.setExpectedDeliveryDate(deliveryDate);
        if (b.getStatus() == null ||
                "WAITING".equalsIgnoreCase(b.getStatus()) ||
                "PENDING".equalsIgnoreCase(b.getStatus()) ||
                "PENDINGAPPROVAL".equalsIgnoreCase(b.getStatus())) {
            b.setStatus("CONFIRMED");
        }
        bookingRepository.save(b);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "id", b.getId(),
                "status", b.getStatus(),
                "expectedDeliveryDate", b.getExpectedDeliveryDate()
        ));
    }

    private long countByStatuses(String... statuses) {
        long total = 0;
        for (String s : statuses) {
            total += bookingRepository.countByStatus(s);
        }
        return total;
    }

    private double sumPaymentsByStatuses(String... statuses) {
        double total = 0;
        for (String s : statuses) {
            List<Payment> payments = paymentRepository.findByStatus(s);
            for (Payment p : payments) {
                total += p.getAmount();
            }
        }
        return total;
    }

    private Map<String, Object> mapBooking(Booking b) {
        Map<String, Object> m = new HashMap<>();
        Customer c = b.getCustomer();
        Car car = b.getCar();
        String carName = "";
        String carImage = "";
        String fuelType = "";
        double price = 0;
        if (car != null) {
            carName = (car.getBrand() == null ? "" : car.getBrand()) +
                    (car.getModel() == null ? "" : " " + car.getModel());
            if (car.getImageUrls() != null && !car.getImageUrls().isEmpty()) {
                carImage = car.getImageUrls().get(0);
            }
            fuelType = car.getFuelType();
            price = car.getPrice();
        }
        m.put("id", b.getId());
        m.put("customerName", c != null ? c.getName() : "N/A");
        m.put("carName", carName.trim());
        m.put("carImage", carImage);
        m.put("fuelType", fuelType);
        m.put("price", price);
        m.put("bookingDate", b.getBookingDate());
        m.put("status", b.getStatus());
        m.put("expectedDeliveryDate", b.getExpectedDeliveryDate());
        return m;
    }
}
