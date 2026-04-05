package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.BankDetail;
import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.Customer;
import com.Dk3.Cars.entity.HomeReview;
import com.Dk3.Cars.entity.LoanDetail;
import com.Dk3.Cars.entity.Notification;
import com.Dk3.Cars.entity.Payment;
import com.Dk3.Cars.entity.PaymentStage;
import com.Dk3.Cars.entity.PaymentTransaction;
import com.Dk3.Cars.entity.Showroom;
import com.Dk3.Cars.entity.TestDrive;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.BankDetailRepository;
import com.Dk3.Cars.repository.BookingRepository;
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.CustomerRepository;
import com.Dk3.Cars.repository.HomeReviewRepository;
import com.Dk3.Cars.repository.LoanDetailRepository;
import com.Dk3.Cars.repository.PaymentStageRepository;
import com.Dk3.Cars.repository.PaymentTransactionRepository;
import com.Dk3.Cars.repository.ShowroomRepository;
import com.Dk3.Cars.repository.UserRepository;
import com.Dk3.Cars.repository.PaymentRepository;
import com.Dk3.Cars.service.BookingService;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.service.NotificationService;
import com.Dk3.Cars.service.PaymentService;
import com.Dk3.Cars.service.TestDriveService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/user-panel")
public class UserPanelController {

    @Autowired
    private CarService carService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private ShowroomRepository showroomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TestDriveService testDriveService;
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
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private HomeReviewRepository homeReviewRepository;

    private boolean isUser(HttpSession session) {
        return "ROLE_USER".equals(session.getAttribute("USER_ROLE"));
    }

    private ResponseEntity<Map<String, Object>> unauthorized() {
        return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Unauthorized"));
    }

    private Optional<User> sessionUser(HttpSession session) {
        Object idObj = session.getAttribute("USER_ID");
        if (idObj == null) return Optional.empty();
        Long userId = Long.valueOf(String.valueOf(idObj));
        return userRepository.findById(userId);
    }

    @GetMapping("/cars")
    public String userCarsPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-cars";
    }

    @GetMapping("/showrooms")
    public String userShowroomsPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-showrooms";
    }

    @GetMapping("/liked-cars")
    public String userLikedCarsPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-liked-cars";
    }

    @GetMapping("/profile")
    public String userProfilePage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-profile";
    }

    @GetMapping("/map")
    public String userMapPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-map";
    }

    @GetMapping("/buy-now")
    public String buyNowPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-buy-now";
    }

    @GetMapping("/payment-success")
    public String paymentSuccessPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-payment-success";
    }

    @GetMapping("/payment-failure")
    public String paymentFailurePage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-payment-failure";
    }

    @GetMapping("/bookings")
    public String userBookingsPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-bookings";
    }

    @GetMapping("/car-details")
    public String userCarDetailsPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-car-details";
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<?> userDashboard(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        List<Booking> bookings = bookingService.getBookingsByCustomerEmail(user.getEmail());
        long pending = bookings.stream().filter(b -> "Pending".equalsIgnoreCase(b.getStatus()) || "Pending".equalsIgnoreCase(b.getWorkflowStatus())).count();
        long approved = bookings.stream().filter(b -> "Approved".equalsIgnoreCase(b.getWorkflowStatus()) || "Confirmed".equalsIgnoreCase(b.getStatus())).count();
        long delivered = bookings.stream().filter(b -> "Delivered".equalsIgnoreCase(b.getWorkflowStatus())).count();

        Map<String, Object> out = new HashMap<>();
        out.put("ok", true);
        out.put("availableCars", carRepository.countAvailableCars());
        out.put("showrooms", showroomRepository.count());
        out.put("bookingsPending", pending);
        out.put("bookingsApproved", approved);
        out.put("bookingsDelivered", delivered);
        out.put("recentBookings", bookings.stream().limit(5).toList());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/api/cars")
    @ResponseBody
    public ResponseEntity<?> getCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "false") boolean includeAll,
            HttpSession session) {
        if (!isUser(session)) return unauthorized();
        List<Car> cars = includeAll ? carService.getAllCars() : carService.getAvailableCars();
        if (cars == null || cars.isEmpty()) {
            cars = carService.getAllCars();
        }
        List<Car> filtered = cars.stream().filter(c -> {
            boolean matchBrand = brand == null || brand.isBlank() || (c.getBrand() != null && c.getBrand().toLowerCase().contains(brand.toLowerCase()));
            boolean matchFuel = fuelType == null || fuelType.isBlank() || fuelType.equalsIgnoreCase(c.getFuelType());
            boolean matchMin = minPrice == null || c.getPrice() >= minPrice;
            boolean matchMax = maxPrice == null || c.getPrice() <= maxPrice;
            return matchBrand && matchFuel && matchMin && matchMax;
        }).toList();
        return ResponseEntity.ok(Map.of("ok", true, "cars", filtered));
    }

    @GetMapping("/api/cars/{id}")
    @ResponseBody
    public ResponseEntity<?> getCarById(@PathVariable Long id, HttpSession session) {
        if (!isUser(session)) return unauthorized();
        return carService.getCarById(id)
                .<ResponseEntity<?>>map(car -> ResponseEntity.ok(Map.of("ok", true, "car", car)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("ok", false, "error", "Car not found")));
    }

    @GetMapping("/api/showrooms")
    @ResponseBody
    public ResponseEntity<?> getShowrooms(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        List<Showroom> showrooms = showroomRepository.findAll();
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : showroomRepository.getAvailableCarCountByShowroom()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Showroom s : showrooms) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("city", s.getCity());
            m.put("address", s.getAddress());
            m.put("contactNumber", s.getContactNumber());
            m.put("type", s.getType());
            m.put("mapUrl", s.getMapUrl());
            m.put("workingHours", s.getWorkingHours());
            m.put("image", s.getImageUrl());
            m.put("imageUrl", s.getImageUrl());
            m.put("availableCarsCount", counts.getOrDefault(s.getId(), 0L));
            out.add(m);
        }
        return ResponseEntity.ok(Map.of("ok", true, "showrooms", out));
    }

    @GetMapping("/api/home-reviews")
    @ResponseBody
    public ResponseEntity<?> myHomeReviews(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        List<Map<String, Object>> reviews = homeReviewRepository.findByUserIdOrderByCreatedAtDesc(user.getUserid())
                .stream()
                .map(this::homeReviewToMap)
                .toList();
        return ResponseEntity.ok(Map.of("ok", true, "reviews", reviews));
    }

    @PostMapping("/api/home-reviews")
    @ResponseBody
    public ResponseEntity<?> submitHomeReview(@RequestBody Map<String, Object> body, HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        String title = trimText(body.get("title"));
        String message = trimText(body.get("message"));
        String city = trimText(body.get("city"));
        String showroomVisited = trimText(body.get("showroomVisited"));
        int rating = parseInteger(body.get("rating"), 0);

        if (title == null || message == null || city == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Title, city, and review message are required"));
        }
        if (rating < 1 || rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Rating must be between 1 and 5"));
        }

        HomeReview review = new HomeReview();
        review.setUserId(user.getUserid());
        review.setUserEmail(user.getEmail());
        review.setReviewerName(resolveUserDisplayName(user));
        review.setCity(city);
        review.setShowroomVisited(showroomVisited);
        review.setTitle(title);
        review.setMessage(message);
        review.setRating(rating);
        review.setApproved(true);

        HomeReview saved = homeReviewRepository.save(review);
        return ResponseEntity.ok(Map.of(
                "ok", true,
                "message", "Your review has been saved and is now visible on the home page.",
                "review", homeReviewToMap(saved)
        ));
    }

    @DeleteMapping("/api/home-reviews/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteHomeReview(@PathVariable Long id, HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        HomeReview review = homeReviewRepository.findById(id).orElse(null);
        if (review == null) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Review not found"));
        }

        boolean ownReview = review.getUserId() != null && review.getUserId().equals(user.getUserid());
        boolean sameEmail = review.getUserEmail() != null && review.getUserEmail().equalsIgnoreCase(user.getEmail());
        if (!ownReview && !sameEmail) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "You can delete only your own reviews"));
        }

        homeReviewRepository.delete(review);
        return ResponseEntity.ok(Map.of("ok", true, "message", "Review deleted successfully"));
    }

    @PostMapping("/api/compare")
    @ResponseBody
    public ResponseEntity<?> compareCars(@RequestParam List<Long> carIds, HttpSession session) {
        if (!isUser(session)) return unauthorized();
        if (carIds == null || carIds.size() != 2) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Select exactly 2 cars for comparison"));
        }
        List<Car> cars = carIds.stream()
                .map(id -> carService.getCarById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        if (cars.size() != 2) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Two valid cars are required for comparison"));
        }

        Car first = cars.get(0);
        Car second = cars.get(1);
        List<Map<String, Object>> comparison = new ArrayList<>();

        int firstScore = 0;
        int secondScore = 0;
        List<String> firstReasons = new ArrayList<>();
        List<String> secondReasons = new ArrayList<>();

        int winnerSide = addComparisonMetric(comparison, "Price", formatCarPrice(first.getPrice()), formatCarPrice(second.getPrice()),
                first.getId(), second.getId(), compareNumbers(first.getPrice(), second.getPrice(), false),
                "Lower price", firstReasons, secondReasons);
        if (winnerSide == 1) firstScore++;
        if (winnerSide == 2) secondScore++;

        winnerSide = addComparisonMetric(comparison, "Mileage", getPrimaryMileage(first), getPrimaryMileage(second),
                first.getId(), second.getId(), compareNumbers(extractLeadingNumber(getPrimaryMileage(first)), extractLeadingNumber(getPrimaryMileage(second)), true),
                "Better mileage", firstReasons, secondReasons);
        if (winnerSide == 1) firstScore++;
        if (winnerSide == 2) secondScore++;

        winnerSide = addComparisonMetric(comparison, "User Review", formatRating(first.getReviewScore()), formatRating(second.getReviewScore()),
                first.getId(), second.getId(), compareNumbers(nullableDouble(first.getReviewScore()), nullableDouble(second.getReviewScore()), true),
                "Better user reviews", firstReasons, secondReasons);
        if (winnerSide == 1) firstScore++;
        if (winnerSide == 2) secondScore++;

        winnerSide = addComparisonMetric(comparison, "Engine", safeText(first.getEngineCc()), safeText(second.getEngineCc()),
                first.getId(), second.getId(), compareNumbers(extractLeadingNumber(first.getEngineCc()), extractLeadingNumber(second.getEngineCc()), true),
                "Stronger engine performance", firstReasons, secondReasons);
        if (winnerSide == 1) firstScore++;
        if (winnerSide == 2) secondScore++;

        winnerSide = addComparisonMetric(comparison, "Safety", safeText(first.getSafetyRating()), safeText(second.getSafetyRating()),
                first.getId(), second.getId(), compareNumbers(extractLeadingNumber(first.getSafetyRating()), extractLeadingNumber(second.getSafetyRating()), true),
                "Higher safety rating", firstReasons, secondReasons);
        if (winnerSide == 1) firstScore++;
        if (winnerSide == 2) secondScore++;

        winnerSide = addComparisonMetric(comparison, "Seating", safeText(first.getSeatingCapacity()), safeText(second.getSeatingCapacity()),
                first.getId(), second.getId(), compareNumbers(extractLeadingNumber(first.getSeatingCapacity()), extractLeadingNumber(second.getSeatingCapacity()), true),
                "More seating capacity", firstReasons, secondReasons);
        if (winnerSide == 1) firstScore++;
        if (winnerSide == 2) secondScore++;

        Map<String, Object> summary = buildCompareSummary(first, second, firstScore, secondScore, firstReasons, secondReasons);
        return ResponseEntity.ok(Map.of("ok", true, "cars", cars, "comparison", comparison, "summary", summary));
    }

    @GetMapping("/api/bookings")
    @ResponseBody
    public ResponseEntity<?> myBookings(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        List<Booking> bookings = bookingService.getBookingsByCustomerEmail(user.getEmail());
        Map<Long, Map<String, String>> stepTracker = new LinkedHashMap<>();
        for (Booking booking : bookings) {
            List<PaymentStage> stages = paymentStageRepository.findByBookingIdOrderByStageOrderNoAsc(booking.getId());
            LoanDetail loan = loanDetailRepository.findByBookingId(booking.getId()).orElse(null);
            stepTracker.put(booking.getId(), computeStepTracker(booking, stages, loan));
        }
        return ResponseEntity.ok(Map.of("ok", true, "bookings", bookings, "stepTracker", stepTracker));
    }

    @DeleteMapping("/api/bookings/{bookingId}/documents/{documentCategory}")
    @ResponseBody
    public ResponseEntity<?> deleteMyBookingDocument(@PathVariable Long bookingId,
                                                     @PathVariable String documentCategory,
                                                     HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }
        String bookingOwnerEmail = booking.getCustomer() != null ? booking.getCustomer().getEmail() : null;
        if (bookingOwnerEmail == null || !bookingOwnerEmail.equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(403).body(Map.of("ok", false, "error", "Access denied"));
        }
        if (!bookingService.isCustomerEditableDocument(documentCategory)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "This document cannot be deleted from the user panel"));
        }

        boolean deleted = bookingService.deleteBookingDocument(booking, documentCategory);
        if (!deleted) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Document not found"));
        }
        return ResponseEntity.ok(Map.of("ok", true, "message", "Document deleted successfully"));
    }

    @GetMapping("/api/payments")
    @ResponseBody
    public ResponseEntity<?> myPayments(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        List<Payment> payments = paymentRepository.findByBookingCustomerEmailOrderByPaymentDateDesc(user.getEmail());
        return ResponseEntity.ok(Map.of("ok", true, "payments", payments));
    }

    @GetMapping("/api/notifications")
    @ResponseBody
    public ResponseEntity<?> myNotifications(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        List<Map<String, Object>> notifications = notificationService.getNotificationsForRecipient(user.getEmail())
                .stream()
                .map(this::notificationToMap)
                .toList();
        return ResponseEntity.ok(Map.of("ok", true, "notifications", notifications));
    }

    @GetMapping("/api/notifications/unread-count")
    @ResponseBody
    public ResponseEntity<?> myNotificationCount(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        return ResponseEntity.ok(Map.of("ok", true, "count", notificationService.countUnreadForRecipient(user.getEmail())));
    }

    @PostMapping("/api/notifications/{id}/mark-read")
    @ResponseBody
    public ResponseEntity<?> markUserNotificationRead(@PathVariable Long id, HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        notificationService.markAsReadForRecipient(id, user.getEmail());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/api/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllUserNotificationsRead(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        notificationService.markAllAsReadForRecipient(user.getEmail());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/api/payment-dashboard")
    @ResponseBody
    public ResponseEntity<?> paymentDashboard(
            @RequestParam(required = false) Long bookingId,
            HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        Booking booking;
        if (bookingId != null) {
            booking = bookingService.getBookingById(bookingId).orElse(null);
            if (booking == null || booking.getCustomer() == null || booking.getCustomer().getEmail() == null
                    || !booking.getCustomer().getEmail().equalsIgnoreCase(user.getEmail())) {
                return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
            }
        } else {
            List<Booking> userBookings = bookingService.getBookingsByCustomerEmail(user.getEmail());
            booking = userBookings.isEmpty() ? null : userBookings.get(0);
            if (booking == null) {
                return ResponseEntity.ok(Map.of("ok", true, "message", "No bookings yet"));
            }
        }

        List<PaymentTransaction> txns = paymentTransactionRepository.findByBookingIdOrderByCreatedAtDesc(booking.getId());
        List<PaymentStage> stages = paymentStageRepository.findByBookingIdOrderByStageOrderNoAsc(booking.getId());
        LoanDetail loan = loanDetailRepository.findByBookingId(booking.getId()).orElse(null);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("bookingId", booking.getId());
        payload.put("car", booking.getCar());
        payload.put("totalAmount", booking.getTotalAmount());
        payload.put("paidAmount", booking.getPaidAmount());
        payload.put("remainingAmount", booking.getRemainingAmount());
        payload.put("paymentOption", booking.getPaymentOption());
        payload.put("paymentGateway", booking.getPaymentGateway());
        payload.put("escrowStatus", booking.getEscrowStatus());
        payload.put("stages", stages);
        payload.put("transactions", txns);
        payload.put("loanDetails", loan);
        payload.put("emiAmount", loan != null ? loan.getEmiAmount() : 0D);
        return ResponseEntity.ok(Map.of("ok", true, "paymentDashboard", payload));
    }

    @GetMapping("/api/banks")
    @ResponseBody
    public ResponseEntity<?> getBanks(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        ensureDefaultBanks();
        return ResponseEntity.ok(Map.of("ok", true, "banks", bankDetailRepository.findByActiveTrueOrderByBankNameAsc()));
    }

    @PostMapping("/api/bookings/{id}/payment-plan")
    @ResponseBody
    public ResponseEntity<?> updatePaymentPlan(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload,
            HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        Booking booking = bookingService.getBookingById(id).orElse(null);
        if (booking == null || booking.getCustomer() == null || booking.getCustomer().getEmail() == null
                || !booking.getCustomer().getEmail().equalsIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Booking not found"));
        }

        String workflow = String.valueOf(booking.getWorkflowStatus() == null ? booking.getStatus() : booking.getWorkflowStatus()).toLowerCase();
        if (workflow.contains("reject") || workflow.contains("cancel") || workflow.contains("deliver")) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Booking is closed"));
        }
        boolean bookingConfirmed = workflow.contains("approved")
                || "confirmed".equalsIgnoreCase(booking.getStatus());
        if (!bookingConfirmed) {
            return ResponseEntity.badRequest().body(Map.of(
                    "ok", false,
                    "error", "Booking is not confirmed yet. Wait for staff verification first."
            ));
        }

        String paymentOption = String.valueOf(payload.getOrDefault("paymentOption", "Full Payment"));
        if (!"Full Payment".equalsIgnoreCase(paymentOption) && !"Loan Required".equalsIgnoreCase(paymentOption)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Payment option must be Full Payment or Loan Required"));
        }
        String loanAadhaarNumber = normalizeDigits(String.valueOf(payload.getOrDefault("loanAadhaarNumber", "")), 12);
        if (!loanAadhaarNumber.isBlank() && !loanAadhaarNumber.matches("\\d{12}")) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Loan Aadhaar number must be exactly 12 digits"));
        }

        double downPayment = parseDouble(payload.get("downPaymentAmount"), 0D);
        if (downPayment < 0) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Down payment cannot be negative"));
        }

        double carPrice = booking.getCar() != null ? booking.getCar().getPrice() : 0D;
        if (downPayment > 0 && carPrice > 0) {
            double minDown = carPrice * 0.10;
            double maxDown = carPrice * 0.30;
            if (downPayment < minDown || downPayment > maxDown) {
                return ResponseEntity.badRequest().body(Map.of(
                        "ok", false,
                        "error", "Down payment must be between 10% and 30% of car price"
                ));
            }
        }

        boolean downPaymentAlreadyVerified = Boolean.TRUE.equals(booking.getDownPaymentVerified());
        if (!downPaymentAlreadyVerified) {
            if (downPayment <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "ok", false,
                        "error", "Please pay down payment first and submit payment reference."
                ));
            }
            booking.setDownPaymentAmount(downPayment);
            booking.setDownPaymentMethod(String.valueOf(payload.getOrDefault("downPaymentMethod", "Bank Transfer")));
            booking.setDownPaymentReference(String.valueOf(payload.getOrDefault("downPaymentReference", "")));
            booking.setDownPaymentVerified(false);
        } else {
            // Lock down payment details after staff verification.
            downPayment = booking.getDownPaymentAmount() == null ? 0D : booking.getDownPaymentAmount();
            booking.setPaymentOption(paymentOption);
        }

        double bookingPaid = downPaymentAlreadyVerified
                ? booking.getBookingAmount() + downPayment
                : booking.getBookingAmount();
        booking.setPaidAmount(bookingPaid);
        double total = booking.getTotalAmount() == null ? 0D : booking.getTotalAmount();
        booking.setRemainingAmount(Math.max(0D, total - booking.getPaidAmount()));
        booking.setStatusUpdatedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        PaymentTransaction existingDownTxn = paymentTransactionRepository.findByBookingIdOrderByCreatedAtDesc(id).stream()
                .filter(t -> t.getPaymentType() != null && "Down Payment".equalsIgnoreCase(t.getPaymentType()))
                .findFirst()
                .orElse(null);
        if (downPayment > 0) {
            PaymentTransaction downTxn = existingDownTxn == null ? new PaymentTransaction() : existingDownTxn;
            downTxn.setBooking(booking);
            downTxn.setPaymentType("Down Payment");
            downTxn.setAmount(downPayment);
            downTxn.setPaymentMethod(booking.getDownPaymentMethod() == null || booking.getDownPaymentMethod().isBlank() ? "Bank Transfer" : booking.getDownPaymentMethod());
            downTxn.setPaymentGateway("Offline");
            downTxn.setTransactionId(booking.getDownPaymentReference());
            downTxn.setReferenceNumber(booking.getDownPaymentReference());
            downTxn.setStatus("Pending");
            downTxn.setNotes("Awaiting manual verification by staff.");
            paymentTransactionRepository.save(downTxn);
        }

        if (downPaymentAlreadyVerified && "Loan Required".equalsIgnoreCase(paymentOption)) {
            LoanDetail loan = loanDetailRepository.findByBookingId(id).orElseGet(LoanDetail::new);
            loan.setBooking(booking);
            loan.setLoanRequired(true);
            loan.setBankName(String.valueOf(payload.getOrDefault("loanBank", loan.getBankName() == null ? "" : loan.getBankName())));
            loan.setSalary(parseDouble(payload.get("salary"), loan.getSalary() == null ? 0D : loan.getSalary()));
            loan.setEmploymentType(String.valueOf(payload.getOrDefault("employmentType", loan.getEmploymentType() == null ? "Salaried" : loan.getEmploymentType())));
            loan.setMonthlyIncome(parseDouble(payload.get("monthlyIncome"), loan.getMonthlyIncome() == null ? 0D : loan.getMonthlyIncome()));
            loan.setPanNumber(String.valueOf(payload.getOrDefault("loanPanNumber", loan.getPanNumber() == null ? "" : loan.getPanNumber())));
            loan.setAadhaarNumber(loanAadhaarNumber.isBlank() ? (loan.getAadhaarNumber() == null ? "" : loan.getAadhaarNumber()) : loanAadhaarNumber);
            loan.setInterestRate(parseDouble(payload.get("loanInterestRate"), loan.getInterestRate() == null ? 9D : loan.getInterestRate()));
            loan.setTenureMonths(parseInteger(payload.get("loanTenureMonths"), loan.getTenureMonths() == null ? 60 : loan.getTenureMonths()));
            loan.setCarPrice(total);
            loan.setDownPaymentAmount(downPayment);
            loan.setLoanAmount(Math.max(0D, total - booking.getBookingAmount() - downPayment));
            loan.setEmiAmount(calculateEmi(loan.getLoanAmount(), loan.getInterestRate(), loan.getTenureMonths()));
            if (loan.getStatus() == null || loan.getStatus().isBlank() || "Rejected".equalsIgnoreCase(loan.getStatus())) {
                loan.setStatus("Pending");
            }
            loanDetailRepository.save(loan);
        }

        if (!downPaymentAlreadyVerified) {
            markPaymentStage(booking, "Down Payment Paid", "Pending",
                    "Down payment submitted by customer. Awaiting staff verification.");
        } else {
            markPaymentStage(booking, "Loan Approved", "Loan Required".equalsIgnoreCase(paymentOption) ? "Pending" : "Not Required",
                    "Loan Required".equalsIgnoreCase(paymentOption)
                            ? "Awaiting bank approval."
                            : "Customer selected full payment. Pay final amount on delivery day.");
        }

        String message = downPaymentAlreadyVerified
                ? "Payment option updated successfully."
                : "Down payment submitted. Staff will verify and then payment option selection will be enabled.";
        return ResponseEntity.ok(Map.of("ok", true, "booking", booking, "message", message));
    }

    @PostMapping("/api/buy-now")
    @ResponseBody
    public ResponseEntity<?> submitBuyNow(
            @RequestParam Long carId,
            @RequestParam String fullName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
            @RequestParam String gender,
            @RequestParam String aadhaarNumber,
            @RequestParam String panNumber,
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String pinCode,
            @RequestParam Double bookingAmount,
            @RequestParam String paymentMode,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String paymentGateway,
            @RequestParam(required = false, defaultValue = "Success") String paymentOutcome,
            @RequestParam(required = false, defaultValue = "Pending Selection") String paymentOption,
            @RequestParam(required = false) Double downPaymentAmount,
            @RequestParam(required = false) String downPaymentMethod,
            @RequestParam(required = false) String downPaymentReference,
            @RequestParam(required = false) Double gstAmount,
            @RequestParam(required = false) Double rtoCharges,
            @RequestParam(required = false) Double roadTaxAmount,
            @RequestParam(required = false) Double insuranceAmount,
            @RequestParam(required = false) Double fastagCharges,
            @RequestParam(required = false) Double handlingCharges,
            @RequestParam(required = false) Double accessoriesAmount,
            @RequestParam(required = false) Double extendedWarrantyAmount,
            @RequestParam(required = false) Double tcsAmount,
            @RequestParam(required = false) String loanBank,
            @RequestParam(required = false) Double salary,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) Double monthlyIncome,
            @RequestParam(required = false) String loanPanNumber,
            @RequestParam(required = false) String loanAadhaarNumber,
            @RequestParam(required = false) Double loanInterestRate,
            @RequestParam(required = false) Integer loanTenureMonths,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedDeliveryDate,
            @RequestParam String deliveryTimeSlot,
            @RequestParam String deliveryType,
            @RequestPart MultipartFile aadhaarPhoto,
            @RequestPart MultipartFile panPhoto,
            @RequestPart MultipartFile signaturePhoto,
            @RequestPart MultipartFile passportPhoto,
            @RequestPart(required = false) MultipartFile paymentScreenshot,
            @RequestPart(required = false) MultipartFile downPaymentReceipt,
            HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        Car car = carService.getCarById(carId).orElse(null);
        if (car == null || !"Available".equalsIgnoreCase(car.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Selected car is not available"));
        }
        if (bookingAmount == null || bookingAmount < 5000 || bookingAmount > 25000) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Booking amount must be between Rs 5,000 and Rs 25,000"));
        }
        if ("Failed".equalsIgnoreCase(paymentOutcome)) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Booking payment failed. Please retry payment."));
        }
        String normalizedAadhaar = normalizeDigits(aadhaarNumber, 12);
        String normalizedPinCode = normalizeDigits(pinCode, 6);
        String normalizedLoanAadhaar = normalizeDigits(loanAadhaarNumber, 12);
        String normalizedCustomerContact = normalizeDigits(user.getContact(), 10);
        if (!normalizedAadhaar.matches("\\d{12}")) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Aadhaar number must be exactly 12 digits"));
        }
        if (!normalizedPinCode.matches("\\d{6}")) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "PIN code must be exactly 6 digits"));
        }
        if (!normalizedCustomerContact.matches("\\d{10}")) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Profile mobile number must be exactly 10 digits before booking"));
        }
        if (normalizedLoanAadhaar != null && !normalizedLoanAadhaar.isBlank() && !normalizedLoanAadhaar.matches("\\d{12}")) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Loan Aadhaar number must be exactly 12 digits"));
        }

        double carPrice = car.getPrice();
        if (downPaymentAmount != null && downPaymentAmount > 0) {
            double minDown = carPrice * 0.10;
            double maxDown = carPrice * 0.30;
            if (downPaymentAmount < minDown || downPaymentAmount > maxDown) {
                return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Down payment must be between 10% and 30% of car price"));
            }
        }

        Customer customer = customerRepository.findByEmail(user.getEmail()).orElseGet(Customer::new);
        customer.setName((user.getFirst() == null ? "" : user.getFirst()) + " " + (user.getLast() == null ? "" : user.getLast()));
        customer.setEmail(user.getEmail());
        customer.setMobile(normalizedCustomerContact);
        customer.setAddress(address);
        if (customer.getLeadSource() == null || customer.getLeadSource().isBlank()) {
            customer.setLeadSource("Website");
        }
        customer = customerRepository.save(customer);

        String resolvedTransactionId = (transactionId == null || transactionId.isBlank())
                ? "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase()
                : transactionId;

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setCar(car);
        booking.setStatus("Pending");
        booking.setWorkflowStatus("Pending");
        booking.setStatusUpdatedAt(LocalDateTime.now());
        booking.setFullName(fullName);
        booking.setDob(dob);
        booking.setGender(gender);
        booking.setAadhaarNumber(normalizedAadhaar);
        booking.setPanNumber(panNumber);
        booking.setAddress(address);
        booking.setCity(city);
        booking.setState(state);
        booking.setPinCode(normalizedPinCode);
        booking.setBookingAmount(bookingAmount);
        booking.setPaymentMode(paymentMode);
        booking.setTransactionId(resolvedTransactionId);
        booking.setPaymentGateway(paymentGateway == null || paymentGateway.isBlank() ? "Razorpay" : paymentGateway);
        booking.setPaymentOutcome(paymentOutcome);
        booking.setPaymentOption("Pending Selection");
        booking.setDownPaymentAmount(downPaymentAmount == null ? 0D : downPaymentAmount);
        booking.setDownPaymentVerified(false);
        booking.setDownPaymentMethod(downPaymentMethod);
        booking.setDownPaymentReference(downPaymentReference);
        booking.setDownPaymentReceiptUrl(saveFile(downPaymentReceipt, "uploads/documents"));
        double computedRoadTax = roundAmount(carPrice * roadTaxRateByState(state));
        double computedRtoCharges = roundAmount(carPrice * 0.0125); // registration + number plate + smart card + misc RTO
        double computedInsurance = roundAmount(Math.max(carPrice * 0.035, 12000D));
        double computedFastag = 500D;
        double computedHandling = 8500D;
        double computedAccessories = roundAmount(Math.max(0D, accessoriesAmount == null ? 0D : accessoriesAmount));
        double computedExtendedWarranty = roundAmount(Math.max(0D, extendedWarrantyAmount == null ? 0D : extendedWarrantyAmount));
        double computedTcs = carPrice > 1_000_000D ? roundAmount(carPrice * 0.01) : 0D;
        double computedGst = roundAmount(Math.max(0D, gstAmount == null ? 0D : gstAmount));

        booking.setGstAmount(computedGst);
        booking.setRtoCharges(computedRtoCharges);
        booking.setRoadTaxAmount(computedRoadTax);
        booking.setInsuranceAmount(computedInsurance);
        booking.setFastagCharges(computedFastag);
        booking.setHandlingCharges(computedHandling);
        booking.setAccessoriesAmount(computedAccessories);
        booking.setExtendedWarrantyAmount(computedExtendedWarranty);
        booking.setTcsAmount(computedTcs);

        double totalAmount = carPrice
                + booking.getGstAmount()
                + booking.getRtoCharges()
                + booking.getRoadTaxAmount()
                + booking.getInsuranceAmount()
                + booking.getFastagCharges()
                + booking.getHandlingCharges()
                + booking.getAccessoriesAmount()
                + booking.getExtendedWarrantyAmount()
                + booking.getTcsAmount();
        double paidAmount = bookingAmount;
        booking.setTotalAmount(totalAmount);
        booking.setPaidAmount(paidAmount);
        booking.setRemainingAmount(Math.max(0D, totalAmount - paidAmount));
        booking.setEscrowStatus("Secured");
        booking.setPaymentScreenshotUrl(saveFile(paymentScreenshot, "uploads/documents"));
        booking.setExpectedDeliveryDate(expectedDeliveryDate);
        booking.setDeliveryTimeSlot(deliveryTimeSlot);
        booking.setDeliveryType(deliveryType);
        booking.setAadhaarPhotoUrl(saveFile(aadhaarPhoto, "uploads/documents"));
        booking.setPanPhotoUrl(saveFile(panPhoto, "uploads/documents"));
        booking.setSignaturePhotoUrl(saveFile(signaturePhoto, "uploads/documents"));
        booking.setPassportPhotoUrl(saveFile(passportPhoto, "uploads/documents"));

        Booking saved = bookingService.saveBooking(booking);
        notificationService.createUserNotification(
                user.getEmail(),
                "Booking Request Sent",
                "Your request for " + (car.getBrand() + " " + car.getModel()).trim()
                        + " on " + expectedDeliveryDate + " during " + deliveryTimeSlot
                        + " has been sent to showroom staff for availability check.",
                "INFO",
                "/user-panel/bookings"
        );

        Payment payment = new Payment();
        payment.setBooking(saved);
        payment.setAmount(bookingAmount);
        payment.setPaymentMethod(paymentMode);
        payment.setTransactionId(resolvedTransactionId);
        payment.setStatus("Completed");
        paymentService.savePayment(payment);

        PaymentTransaction bookingTx = new PaymentTransaction();
        bookingTx.setBooking(saved);
        bookingTx.setPaymentType("Booking");
        bookingTx.setAmount(bookingAmount);
        bookingTx.setPaymentMethod(paymentMode);
        bookingTx.setPaymentGateway(saved.getPaymentGateway());
        bookingTx.setTransactionId(resolvedTransactionId);
        bookingTx.setReferenceNumber(resolvedTransactionId);
        bookingTx.setReceiptUrl(saved.getPaymentScreenshotUrl());
        bookingTx.setStatus("Completed");
        bookingTx.setNotes("Booking amount paid online.");
        paymentTransactionRepository.save(bookingTx);

        if (downPaymentAmount != null && downPaymentAmount > 0) {
            PaymentTransaction dpTx = new PaymentTransaction();
            dpTx.setBooking(saved);
            dpTx.setPaymentType("Down Payment");
            dpTx.setAmount(downPaymentAmount);
            dpTx.setPaymentMethod(downPaymentMethod == null || downPaymentMethod.isBlank() ? "Bank Transfer" : downPaymentMethod);
            dpTx.setPaymentGateway("Offline");
            dpTx.setTransactionId(downPaymentReference);
            dpTx.setReferenceNumber(downPaymentReference);
            dpTx.setReceiptUrl(saved.getDownPaymentReceiptUrl());
            dpTx.setStatus("Pending");
            dpTx.setNotes("Awaiting manual verification by staff.");
            paymentTransactionRepository.save(dpTx);
        }

        if ("Loan Required".equalsIgnoreCase(booking.getPaymentOption())) {
            LoanDetail loan = new LoanDetail();
            loan.setBooking(saved);
            loan.setLoanRequired(true);
            loan.setBankName(loanBank);
            loan.setSalary(salary);
            loan.setEmploymentType(employmentType);
            loan.setMonthlyIncome(monthlyIncome);
            loan.setPanNumber(loanPanNumber);
            loan.setAadhaarNumber(normalizedLoanAadhaar);
            loan.setInterestRate(loanInterestRate == null ? 9.0 : loanInterestRate);
            loan.setTenureMonths(loanTenureMonths == null || loanTenureMonths <= 0 ? 60 : loanTenureMonths);
            loan.setCarPrice(carPrice);
            loan.setDownPaymentAmount(downPaymentAmount == null ? 0D : downPaymentAmount);
            loan.setLoanAmount(Math.max(0D, carPrice - (downPaymentAmount == null ? 0D : downPaymentAmount)));
            loan.setEmiAmount(calculateEmi(loan.getLoanAmount(), loan.getInterestRate(), loan.getTenureMonths()));
            loan.setStatus("Pending");
            loanDetailRepository.save(loan);
        }

        createPaymentStages(saved, downPaymentAmount != null && downPaymentAmount > 0, "Loan Required".equalsIgnoreCase(saved.getPaymentOption()));
        bookingService.sendInitialBookingEmailPack(saved.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("ok", true);
        response.put("bookingId", saved.getId());
        response.put("status", saved.getWorkflowStatus());
        response.put("roadTaxAmount", saved.getRoadTaxAmount());
        response.put("rtoCharges", saved.getRtoCharges());
        response.put("insuranceAmount", saved.getInsuranceAmount());
        response.put("fastagCharges", saved.getFastagCharges());
        response.put("handlingCharges", saved.getHandlingCharges());
        response.put("accessoriesAmount", saved.getAccessoriesAmount());
        response.put("extendedWarrantyAmount", saved.getExtendedWarrantyAmount());
        response.put("tcsAmount", saved.getTcsAmount());
        response.put("totalAmount", saved.getTotalAmount());
        response.put("paidAmount", saved.getPaidAmount());
        response.put("remainingAmount", saved.getRemainingAmount());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/test-drive")
    @ResponseBody
    public ResponseEntity<?> bookTestDrive(
            @RequestParam Long carId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime scheduledDateTime,
            HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        Car car = carService.getCarById(carId).orElse(null);
        if (car == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "Car not found"));
        Customer customer = customerRepository.findByEmail(user.getEmail()).orElse(null);
        if (customer == null) return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Customer profile not found"));

        TestDrive td = new TestDrive();
        td.setCar(car);
        td.setCustomer(customer);
        td.setStatus("Scheduled");
        td.setScheduledDateTime(scheduledDateTime);
        testDriveService.saveTestDrive(td);

        return ResponseEntity.ok(Map.of("ok", true, "message", "Test drive scheduled"));
    }

    private String saveFile(MultipartFile file, String folderPath) {
        if (file == null || file.isEmpty()) return null;
        try {
            Path folder = Path.of(folderPath);
            Files.createDirectories(folder);
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0) ext = original.substring(dot);
            String fileName = UUID.randomUUID() + ext;
            Path target = folder.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/" + folderPath.replace("\\", "/") + "/" + fileName;
        } catch (IOException e) {
            return null;
        }
    }

    private void createPaymentStages(Booking booking, boolean downPaymentSubmitted, boolean loanRequired) {
        List<PaymentStage> stages = new ArrayList<>();
        stages.add(stage(booking, 1, "Booking Paid", "Completed", "Booking amount received."));
        stages.add(stage(booking, 2, "Down Payment Paid", "Pending",
                downPaymentSubmitted ? "Awaiting staff verification for submitted down payment." : "Awaiting down payment."));
        stages.add(stage(booking, 3, "Loan Approved", loanRequired ? "Pending" : "Not Required",
                loanRequired ? "Awaiting bank approval." : "Customer selected full payment."));
        stages.add(stage(booking, 4, "Final Amount Received", "Pending", "Awaiting final settlement."));
        stages.add(stage(booking, 5, "Delivery Ready", "Pending", "Delivery will be prepared after payments."));
        paymentStageRepository.saveAll(stages);
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
        if (Boolean.TRUE.equals(booking.getDownPaymentVerified())) {
            step2Status = "Completed";
        } else if (downStage != null && downStage.getStageStatus() != null && !downStage.getStageStatus().isBlank()) {
            step2Status = normalizeStatus(downStage.getStageStatus());
        }

        if (!Boolean.TRUE.equals(booking.getDownPaymentVerified())) {
            step3Label = "Payment Plan Selection";
            step3Status = "Pending";
        } else if ("Loan Required".equalsIgnoreCase(booking.getPaymentOption())) {
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
            step3Label = "Full Payment on Delivery Day";
            if (booking.getRemainingAmount() != null && booking.getRemainingAmount() <= 0) {
                step3Status = "Completed";
            } else {
                step3Status = "Pending";
            }
        }

        Map<String, String> out = new LinkedHashMap<>();
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

    private PaymentStage stage(Booking booking, int order, String name, String status, String remarks) {
        PaymentStage stage = new PaymentStage();
        stage.setBooking(booking);
        stage.setStageOrderNo(order);
        stage.setStageName(name);
        stage.setStageStatus(status);
        stage.setRemarks(remarks);
        return stage;
    }

    private double calculateEmi(double principal, double annualRate, int months) {
        if (principal <= 0 || annualRate <= 0 || months <= 0) return 0D;
        double monthlyRate = annualRate / (12 * 100);
        double factor = Math.pow(1 + monthlyRate, months);
        return (principal * monthlyRate * factor) / (factor - 1);
    }

    private void markPaymentStage(Booking booking, String stageName, String stageStatus, String remarks) {
        if (booking == null || booking.getId() == null) return;
        paymentStageRepository.findByBookingIdOrderByStageOrderNoAsc(booking.getId()).stream()
                .filter(s -> s.getStageName() != null && s.getStageName().equalsIgnoreCase(stageName))
                .findFirst()
                .ifPresent(s -> {
                    s.setStageStatus(stageStatus);
                    s.setRemarks(remarks);
                    paymentStageRepository.save(s);
                });
    }

    private double parseDouble(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private int parseInteger(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private String normalizeDigits(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String digits = value.replaceAll("\\D", "");
        return digits.length() > maxLength ? digits.substring(0, maxLength) : digits;
    }

    private Map<String, Object> buildCompareSummary(Car first, Car second, int firstScore, int secondScore,
                                                    List<String> firstReasons, List<String> secondReasons) {
        Map<String, Object> summary = new LinkedHashMap<>();
        boolean draw = firstScore == secondScore;
        Car winner = draw ? null : (firstScore > secondScore ? first : second);
        List<String> reasons = draw ? List.of("Both cars are closely matched on the selected specifications.")
                : trimReasons(firstScore > secondScore ? firstReasons : secondReasons);

        summary.put("draw", draw);
        summary.put("firstScore", firstScore);
        summary.put("secondScore", secondScore);
        summary.put("winnerId", winner == null ? null : winner.getId());
        summary.put("winnerName", winner == null ? "Both cars are evenly matched" : carDisplayName(winner));
        summary.put("headline", draw
                ? "Both cars are almost equally strong choices."
                : carDisplayName(winner) + " looks better overall for this comparison.");
        summary.put("reasons", reasons);
        return summary;
    }

    private List<String> trimReasons(List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) return List.of("Balanced overall performance.");
        return reasons.stream().distinct().limit(3).toList();
    }

    private int addComparisonMetric(List<Map<String, Object>> comparison, String label, String firstValue, String secondValue,
                                    Long firstId, Long secondId, int winnerSide, String winnerReason,
                                    List<String> firstReasons, List<String> secondReasons) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("label", label);
        row.put("firstValue", firstValue);
        row.put("secondValue", secondValue);
        row.put("winner", winnerSide == 1 ? firstId : winnerSide == 2 ? secondId : null);
        comparison.add(row);

        if (winnerSide == 1) {
            firstReasons.add(winnerReason);
            return 1;
        }
        if (winnerSide == 2) {
            secondReasons.add(winnerReason);
            return 2;
        }
        return 0;
    }

    private int compareNumbers(Double first, Double second, boolean higherIsBetter) {
        if (first == null && second == null) return 0;
        if (first != null && second == null) return 1;
        if (first == null) return 2;
        int compared = Double.compare(first, second);
        if (compared == 0) return 0;
        if (higherIsBetter) return compared > 0 ? 1 : 2;
        return compared < 0 ? 1 : 2;
    }

    private Double extractLeadingNumber(String value) {
        if (value == null || value.isBlank()) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(value.replace(",", ""));
        if (!matcher.find()) return null;
        try {
            return Double.parseDouble(matcher.group(1));
        } catch (Exception ex) {
            return null;
        }
    }

    private String formatCarPrice(double value) {
        if (value <= 0) return "Price on request";
        if (value >= 10000000) return String.format("Rs %.2f Crore", value / 10000000D);
        return String.format("Rs %.2f Lakh", value / 100000D);
    }

    private String getPrimaryMileage(Car car) {
        if (car == null) return "N/A";
        if (car.getMileage() != null && !car.getMileage().isBlank()) return car.getMileage();
        if (car.getMileageDetails() == null || car.getMileageDetails().isBlank()) return "N/A";
        String firstRow = car.getMileageDetails().split("\\|")[0];
        String[] parts = firstRow.split(":");
        return parts.length >= 2 ? parts[1].trim() : firstRow.trim();
    }

    private String formatRating(Double score) {
        if (score == null || score <= 0) return "N/A";
        return String.format("%.1f/5", score);
    }

    private Double nullableDouble(Double value) {
        return value == null || value <= 0 ? null : value;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String carDisplayName(Car car) {
        if (car == null) return "Selected car";
        return ((car.getBrand() == null ? "" : car.getBrand()) + " " + (car.getModel() == null ? "" : car.getModel())).trim();
    }

    private double roadTaxRateByState(String state) {
        String s = state == null ? "" : state.trim().toLowerCase();
        if (s.contains("maharashtra")) return 0.11;
        if (s.contains("karnataka")) return 0.13;
        if (s.contains("delhi")) return 0.10;
        if (s.contains("gujarat")) return 0.08;
        if (s.contains("tamil nadu") || s.contains("tamilnadu")) return 0.10;
        if (s.contains("telangana")) return 0.10;
        return 0.10;
    }

    private double roundAmount(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Map<String, Object> notificationToMap(Notification notification) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", notification.getId());
        map.put("title", notification.getTitle());
        map.put("message", notification.getMessage());
        map.put("type", notification.getType());
        map.put("readFlag", notification.isReadFlag());
        map.put("link", notification.getLink());
        map.put("createdAt", notification.getCreatedAt());
        return map;
    }

    private Map<String, Object> homeReviewToMap(HomeReview review) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", review.getId());
        map.put("reviewerName", review.getReviewerName());
        map.put("city", review.getCity());
        map.put("showroomVisited", review.getShowroomVisited());
        map.put("title", review.getTitle());
        map.put("message", review.getMessage());
        map.put("rating", review.getRating());
        map.put("createdAt", review.getCreatedAt());
        return map;
    }

    private String resolveUserDisplayName(User user) {
        if (user == null) return "DK3 Customer";
        String fullName = ((user.getFirst() == null ? "" : user.getFirst()) + " "
                + (user.getLast() == null ? "" : user.getLast())).trim();
        if (!fullName.isBlank()) return fullName;
        if (user.getEmail() != null && user.getEmail().contains("@")) {
            return user.getEmail().substring(0, user.getEmail().indexOf('@'));
        }
        return "DK3 Customer";
    }

    private String trimText(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private void ensureDefaultBanks() {
        if (bankDetailRepository.count() > 0) return;
        List<BankDetail> banks = new ArrayList<>();
        banks.add(bank("State Bank of India", "DK3 Cars Pvt Ltd", "XXXXXX5678", "SBIN0000456", "Nashik Main"));
        banks.add(bank("HDFC Bank", "DK3 Cars Pvt Ltd", "XXXXXX4321", "HDFC0001984", "College Road"));
        banks.add(bank("ICICI Bank", "DK3 Cars Pvt Ltd", "XXXXXX8899", "ICIC0000371", "CIDCO"));
        bankDetailRepository.saveAll(banks);
    }

    private BankDetail bank(String name, String holder, String accountMasked, String ifsc, String branch) {
        BankDetail bank = new BankDetail();
        bank.setBankName(name);
        bank.setAccountHolderName(holder);
        bank.setAccountNumberMasked(accountMasked);
        bank.setIfscCode(ifsc);
        bank.setBranchName(branch);
        bank.setActive(true);
        return bank;
    }
}
