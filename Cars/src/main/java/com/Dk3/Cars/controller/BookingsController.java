package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.service.BookingService;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.repository.CustomerRepository;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    @GetMapping
    public String bookingsPage(Model model) {

        model.addAttribute("customers", customerRepository.findAll());
        model.addAttribute("cars", carService.getAvailableCars());
        model.addAttribute("salesExecutives", userRepository.findActiveSalesExecutives());
        return "bookings";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PostMapping("/api")
    @ResponseBody
    public Map<String, Object> createBooking(@RequestBody Map<String, Object> bookingData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = new Booking();
            if (bookingData.get("customerId") != null) {
                booking.setCustomer(customerRepository.findById(Long.valueOf(bookingData.get("customerId").toString())).orElse(null));
            }
            booking.setCar(carService.getCarById(Long.valueOf(bookingData.get("carId").toString())).orElse(null));
            
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
    public Map<String, Object> updateBooking(@PathVariable Long id, @RequestBody Map<String, Object> bookingData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingService.getBookingById(id).orElse(null);
            if (booking == null) {
                response.put("success", false);
                response.put("error", "Booking not found");
                return response;
            }

            if (bookingData.containsKey("customerId")) {
                booking.setCustomer(customerRepository.findById(Long.valueOf(bookingData.get("customerId").toString())).orElse(null));
            }
            if (bookingData.containsKey("carId")) {
                booking.setCar(carService.getCarById(Long.valueOf(bookingData.get("carId").toString())).orElse(null));
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
    public Map<String, Object> deleteBooking(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
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
    public Map<String, Object> getBooking(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Booking booking = bookingService.getBookingById(id).orElse(null);
            if (booking == null) {
                response.put("success", false);
                response.put("error", "Booking not found");
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
