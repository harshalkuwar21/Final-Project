package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.service.BookingService;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.repository.CustomerRepository;
import com.Dk3.Cars.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Controller
@RequestMapping("/bookings")
public class BookingsController {

    @Autowired
    private BookingService bookingService;

    

    @Autowired
    private CarService carService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private boolean isStaffRole(String role) {
        return role != null && !"ROLE_ADMIN".equals(role) && !"ROLE_USER".equals(role);
    }

    private Optional<User> getSessionUser(HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) return Optional.empty();
        Long userId = Long.valueOf(String.valueOf(userIdObj));
        return userRepository.findById(userId);
    }

    private boolean canAccessBooking(HttpSession session, Booking booking) {
        String role = (String) session.getAttribute("USER_ROLE");
        if (!isStaffRole(role)) return true;
        Long staffShowroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
        Long bookingShowroomId = booking != null && booking.getCar() != null && booking.getCar().getShowroom() != null
                ? booking.getCar().getShowroom().getId() : null;
        return staffShowroomId != null && staffShowroomId.equals(bookingShowroomId);
    }

    @GetMapping
    public String bookingsPage(Model model, HttpSession session) {
        String role = (String) session.getAttribute("USER_ROLE");
        boolean staff = isStaffRole(role);

        if (staff) {
            Long showroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
            if (showroomId == null) {
                model.addAttribute("customers", java.util.Collections.emptyList());
                model.addAttribute("cars", java.util.Collections.emptyList());
                model.addAttribute("salesExecutives", java.util.Collections.emptyList());
                model.addAttribute("error", "No showroom is assigned to this staff account.");
            } else {
                model.addAttribute("customers", customerRepository.findAll());
                model.addAttribute("cars", carService.getCarRepository().findByShowroomIdOrderByIdDesc(showroomId));
                model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
                model.addAttribute("staffShowroomId", showroomId);
            }
        } else {
            model.addAttribute("customers", customerRepository.findAll());
            model.addAttribute("cars", carService.getAvailableCars());
            model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
        }
        return "bookings";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<Booking> getAllBookings(HttpSession session) {
        String role = (String) session.getAttribute("USER_ROLE");
        if (!isStaffRole(role)) {
            return bookingService.getAllBookings();
        }
        Long showroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
        if (showroomId == null) {
            return java.util.Collections.emptyList();
        }
        return bookingService.getAllBookings()
                .stream()
                .filter(b -> b.getCar() != null && b.getCar().getShowroom() != null
                        && showroomId.equals(b.getCar().getShowroom().getId()))
                .toList();
    }

    @PostMapping("/api")
    @ResponseBody
    public Map<String, Object> createBooking(@RequestBody Map<String, Object> bookingData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = new Booking();
            if (bookingData.get("customerId") != null) {
                booking.setCustomer(customerRepository.findById(Long.valueOf(bookingData.get("customerId").toString())).orElse(null));
            }
            booking.setCar(carService.getCarById(Long.valueOf(bookingData.get("carId").toString())).orElse(null));
            if (!canAccessBooking(session, booking)) {
                response.put("success", false);
                response.put("error", "Access denied for this booking");
                return response;
            }
            
            // Handle sales executive assignment
            if (bookingData.get("salesExecutiveId") != null && !bookingData.get("salesExecutiveId").toString().isEmpty()) {
                booking.setSalesExecutive(userRepository.findById(Long.valueOf(bookingData.get("salesExecutiveId").toString())).orElse(null));
            }
            
            booking.setStatus((String) bookingData.get("status"));
            booking.setBookingAmount(Double.valueOf(bookingData.get("bookingAmount").toString()));
            booking.setPaymentMode((String) bookingData.get("paymentMode"));
            if (bookingData.get("expectedDeliveryDate") != null && !bookingData.get("expectedDeliveryDate").toString().isEmpty()) {
                booking.setExpectedDeliveryDate(java.time.LocalDate.parse(bookingData.get("expectedDeliveryDate").toString()));
            }

            Booking saved = bookingService.saveBooking(booking);
            response.put("success", true);
            response.put("booking", saved);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @PutMapping("/api/{id}")
    @ResponseBody
    public Map<String, Object> updateBooking(@PathVariable Long id, @RequestBody Map<String, Object> bookingData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingService.getBookingById(id).orElse(null);
            if (booking == null) {
                response.put("success", false);
                response.put("error", "Booking not found");
                return response;
            }
            if (!canAccessBooking(session, booking)) {
                response.put("success", false);
                response.put("error", "Access denied for this booking");
                return response;
            }

            if (bookingData.containsKey("customerId")) {
                booking.setCustomer(customerRepository.findById(Long.valueOf(bookingData.get("customerId").toString())).orElse(null));
            }
            if (bookingData.containsKey("carId")) {
                booking.setCar(carService.getCarById(Long.valueOf(bookingData.get("carId").toString())).orElse(null));
                if (!canAccessBooking(session, booking)) {
                    response.put("success", false);
                    response.put("error", "Access denied for this booking");
                    return response;
                }
            }
            if (bookingData.containsKey("salesExecutiveId")) {
                String salesExecId = (String) bookingData.get("salesExecutiveId");
                if (salesExecId != null && !salesExecId.isEmpty()) {
                    booking.setSalesExecutive(userRepository.findById(Long.valueOf(salesExecId)).orElse(null));
                } else {
                    booking.setSalesExecutive(null);
                }
            }
            if (bookingData.containsKey("status")) {
                booking.setStatus((String) bookingData.get("status"));
            }
            if (bookingData.containsKey("bookingAmount")) {
                booking.setBookingAmount(Double.valueOf(bookingData.get("bookingAmount").toString()));
            }
            if (bookingData.containsKey("paymentMode")) {
                booking.setPaymentMode((String) bookingData.get("paymentMode"));
            }
            if (bookingData.containsKey("expectedDeliveryDate")) {
                String dateStr = (String) bookingData.get("expectedDeliveryDate");
                if (dateStr != null && !dateStr.isEmpty()) {
                    booking.setExpectedDeliveryDate(java.time.LocalDate.parse(dateStr));
                }
            }

            Booking saved = bookingService.saveBooking(booking);
            response.put("success", true);
            response.put("booking", saved);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public Map<String, Object> deleteBooking(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingService.getBookingById(id).orElse(null);
            if (booking == null) {
                response.put("success", false);
                response.put("error", "Booking not found");
                return response;
            }
            if (!canAccessBooking(session, booking)) {
                response.put("success", false);
                response.put("error", "Access denied for this booking");
                return response;
            }
            bookingService.deleteBooking(id);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Map<String, Object> getBooking(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingService.getBookingById(id).orElse(null);
            if (booking == null) {
                response.put("success", false);
                response.put("error", "Booking not found");
            } else if (!canAccessBooking(session, booking)) {
                response.put("success", false);
                response.put("error", "Access denied for this booking");
            } else {
                response.put("success", true);
                response.put("booking", booking);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }
}
