package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.LoanDetail;
import com.Dk3.Cars.entity.Payment;
import com.Dk3.Cars.entity.PaymentStage;
import com.Dk3.Cars.entity.PaymentTransaction;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.BankDetailRepository;
import com.Dk3.Cars.repository.CustomerRepository;
import com.Dk3.Cars.repository.LoanDetailRepository;
import com.Dk3.Cars.repository.PaymentRepository;
import com.Dk3.Cars.repository.PaymentStageRepository;
import com.Dk3.Cars.repository.PaymentTransactionRepository;
import com.Dk3.Cars.repository.UserRepository;
import com.Dk3.Cars.service.BookingService;
import com.Dk3.Cars.service.CarService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private PaymentStageRepository paymentStageRepository;

    @Autowired
    private LoanDetailRepository loanDetailRepository;

    @Autowired
    private BankDetailRepository bankDetailRepository;

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

    @GetMapping("/details/{id}")
    public String bookingDocumentsDetailsPage(@PathVariable Long id, Model model, HttpSession session) {
        Booking booking = bookingService.getBookingById(id).orElse(null);
        if (booking == null || !canAccessBooking(session, booking)) {
            return "redirect:/bookings";
        }

        List<Payment> payments = paymentRepository.findByBookingId(id);
        List<PaymentTransaction> paymentTransactions = paymentTransactionRepository.findByBookingIdOrderByCreatedAtDesc(id);
        LoanDetail loanDetail = loanDetailRepository.findByBookingId(id).orElse(null);
        List<PaymentStage> stages = paymentStageRepository.findByBookingIdOrderByStageOrderNoAsc(id);

        model.addAttribute("booking", booking);
        model.addAttribute("payments", payments);
        model.addAttribute("paymentTransactions", paymentTransactions);
        model.addAttribute("loanDetail", loanDetail);
        model.addAttribute("stepTracker", computeStepTracker(booking, stages, loanDetail));
        model.addAttribute("activeBanks", bankDetailRepository.findByActiveTrueOrderByBankNameAsc());

        return "booking-documents-details";
    }

    private Map<String, String> computeStepTracker(Booking booking, List<PaymentStage> stages, LoanDetail loan) {
        String step1Status = "Completed";
        String step2Status = "Pending";
        String step3Label = "Full Payment Paid";
        String step3Status = "Pending";

        PaymentStage bookingStage = findStage(stages, "Booking Paid");
        if (bookingStage != null && bookingStage.getStageStatus() != null && !bookingStage.getStageStatus().isBlank()) {
            step1Status = normalizeStatus(bookingStage.getStageStatus());
        } else if (booking.getBookingAmount() <= 0) {
            step1Status = "Pending";
        }

        PaymentStage downStage = findStage(stages, "Down Payment Paid");
        if (downStage != null && downStage.getStageStatus() != null && !downStage.getStageStatus().isBlank()) {
            step2Status = normalizeStatus(downStage.getStageStatus());
        } else if (booking.getDownPaymentAmount() != null && booking.getDownPaymentAmount() > 0) {
            step2Status = "Completed";
        } else if (!"Loan Required".equalsIgnoreCase(booking.getPaymentOption())) {
            step2Status = "Not Required";
        }

        if ("Loan Required".equalsIgnoreCase(booking.getPaymentOption())) {
            step3Label = "Loan Approved";
            if (loan == null || loan.getStatus() == null || loan.getStatus().isBlank()) {
                step3Status = "Pending";
            } else if ("Approved".equalsIgnoreCase(loan.getStatus())) {
                step3Status = "Completed";
            } else if ("Rejected".equalsIgnoreCase(loan.getStatus())) {
                step3Status = "Rejected";
            } else {
                step3Status = "Pending";
            }
        } else {
            PaymentStage finalStage = findStage(stages, "Final Amount Received");
            if (finalStage != null && finalStage.getStageStatus() != null && !finalStage.getStageStatus().isBlank()) {
                step3Status = normalizeStatus(finalStage.getStageStatus());
            } else if (booking.getRemainingAmount() != null && booking.getRemainingAmount() <= 0) {
                step3Status = "Completed";
            }
        }

        Map<String, String> out = new HashMap<>();
        out.put("step1Label", "Booking Amount Paid");
        out.put("step1Status", step1Status);
        out.put("step2Label", "Down Payment Paid");
        out.put("step2Status", step2Status);
        out.put("step3Label", step3Label);
        out.put("step3Status", step3Status);
        return out;
    }

    private PaymentStage findStage(List<PaymentStage> stages, String stageName) {
        if (stages == null || stageName == null) return null;
        return stages.stream()
                .filter(s -> s.getStageName() != null && s.getStageName().equalsIgnoreCase(stageName))
                .findFirst()
                .orElse(null);
    }

    private String normalizeStatus(String status) {
        if (status == null) return "Pending";
        if ("Completed".equalsIgnoreCase(status)) return "Completed";
        if ("Rejected".equalsIgnoreCase(status)) return "Rejected";
        if ("Not Required".equalsIgnoreCase(status)) return "Not Required";
        return "Pending";
    }

    @GetMapping("/api")
    @ResponseBody
    public List<Booking> getAllBookings(HttpSession session) {
        return getBookingsByDeliveryState(session, false);
    }

    @GetMapping("/api/delivered")
    @ResponseBody
    public List<Booking> getDeliveredBookings(HttpSession session) {
        return getBookingsByDeliveryState(session, true);
    }

    private List<Booking> getBookingsByDeliveryState(HttpSession session, boolean delivered) {
        String role = (String) session.getAttribute("USER_ROLE");
        return bookingService.getAllBookings().stream()
                .filter(b -> delivered == isDelivered(b))
                .filter(b -> {
                    if (!isStaffRole(role)) return true;
                    Long showroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
                    return showroomId != null
                            && b.getCar() != null
                            && b.getCar().getShowroom() != null
                            && showroomId.equals(b.getCar().getShowroom().getId());
                })
                .toList();
    }

    private boolean isDelivered(Booking booking) {
        String s = String.valueOf(booking.getWorkflowStatus() == null ? booking.getStatus() : booking.getWorkflowStatus()).toLowerCase();
        return s.contains("deliver");
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
