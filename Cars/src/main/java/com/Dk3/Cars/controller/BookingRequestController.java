package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.Customer;
import com.Dk3.Cars.service.BookingService;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.repository.CustomerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class BookingRequestController {

    private static final String SESSION_ROLE = "USER_ROLE";

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CarService carService;

    @Autowired
    private CustomerRepository customerRepository;

    private String redirectByRole(String role) {
        if ("ROLE_ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        if ("ROLE_USER".equals(role)) {
            return "booking-request";
        }
        return "redirect:/staff-dashboard";
    }

    @GetMapping("/booking-request")
    public String bookingRequestPage(HttpSession session, Model model) {
        String role = (String) session.getAttribute(SESSION_ROLE);
        if (role == null) {
            return "redirect:/login";
        }
        if (!"ROLE_USER".equals(role)) {
            return redirectByRole(role);
        }
        model.addAttribute("cars", carService.getAvailableCars());
        return "booking-request";
    }

    @PostMapping("/booking-request/api")
    @ResponseBody
    public Map<String, Object> submitBookingRequest(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            String name = payload.get("name") != null ? payload.get("name").toString().trim() : "";
            String email = payload.get("email") != null ? payload.get("email").toString().trim() : "";
            String mobile = payload.get("mobile") != null ? payload.get("mobile").toString().trim() : "";
            String address = payload.get("address") != null ? payload.get("address").toString().trim() : "";
            String paymentMode = payload.get("paymentMode") != null ? payload.get("paymentMode").toString().trim() : "";

            if (name.isEmpty() || email.isEmpty() || mobile.isEmpty()) {
                response.put("success", false);
                response.put("error", "Name, email, and mobile are required.");
                return response;
            }
            if (payload.get("carId") == null) {
                response.put("success", false);
                response.put("error", "Car selection is required.");
                return response;
            }

            Customer customer = customerRepository.findByEmail(email).orElseGet(Customer::new);
            customer.setName(name);
            customer.setEmail(email);
            customer.setMobile(mobile);
            customer.setAddress(address);
            if (customer.getLeadSource() == null || customer.getLeadSource().isBlank()) {
                customer.setLeadSource("Website");
            }
            customer = customerRepository.save(customer);

            Booking booking = new Booking();
            booking.setCustomer(customer);
            booking.setCar(carService.getCarById(Long.valueOf(payload.get("carId").toString())).orElse(null));
            booking.setStatus("WAITING");
            if (payload.get("bookingAmount") != null && !payload.get("bookingAmount").toString().isBlank()) {
                booking.setBookingAmount(Double.valueOf(payload.get("bookingAmount").toString()));
            }
            booking.setPaymentMode(paymentMode.isEmpty() ? "Pending" : paymentMode);

            Booking saved = bookingService.saveBooking(booking);
            response.put("success", true);
            response.put("bookingId", saved.getId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
