package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Booking;
import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.Customer;
import com.Dk3.Cars.entity.Payment;
import com.Dk3.Cars.entity.Showroom;
import com.Dk3.Cars.entity.TestDrive;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.BookingRepository;
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.CustomerRepository;
import com.Dk3.Cars.repository.ShowroomRepository;
import com.Dk3.Cars.repository.UserRepository;
import com.Dk3.Cars.repository.PaymentRepository;
import com.Dk3.Cars.service.BookingService;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.service.PaymentService;
import com.Dk3.Cars.service.TestDriveService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/buy-now")
    public String buyNowPage(HttpSession session) {
        if (!isUser(session)) return "redirect:/login";
        return "user-buy-now";
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
            m.put("mapUrl", s.getMapUrl());
            m.put("workingHours", s.getWorkingHours());
            m.put("availableCarsCount", counts.getOrDefault(s.getId(), 0L));
            out.add(m);
        }
        return ResponseEntity.ok(Map.of("ok", true, "showrooms", out));
    }

    @PostMapping("/api/compare")
    @ResponseBody
    public ResponseEntity<?> compareCars(@RequestParam List<Long> carIds, HttpSession session) {
        if (!isUser(session)) return unauthorized();
        if (carIds == null || carIds.size() < 2) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Select at least 2 cars for comparison"));
        }
        List<Car> cars = carIds.stream()
                .map(id -> carService.getCarById(id).orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
        return ResponseEntity.ok(Map.of("ok", true, "cars", cars));
    }

    @GetMapping("/api/bookings")
    @ResponseBody
    public ResponseEntity<?> myBookings(HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        List<Booking> bookings = bookingService.getBookingsByCustomerEmail(user.getEmail());
        return ResponseEntity.ok(Map.of("ok", true, "bookings", bookings));
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
            @RequestParam String transactionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expectedDeliveryDate,
            @RequestParam String deliveryTimeSlot,
            @RequestParam String deliveryType,
            @RequestPart MultipartFile aadhaarPhoto,
            @RequestPart MultipartFile panPhoto,
            @RequestPart MultipartFile signaturePhoto,
            @RequestPart MultipartFile passportPhoto,
            @RequestPart MultipartFile paymentScreenshot,
            HttpSession session) {
        if (!isUser(session)) return unauthorized();
        User user = sessionUser(session).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));

        Car car = carService.getCarById(carId).orElse(null);
        if (car == null || !"Available".equalsIgnoreCase(car.getStatus())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Selected car is not available"));
        }

        Customer customer = customerRepository.findByEmail(user.getEmail()).orElseGet(Customer::new);
        customer.setName((user.getFirst() == null ? "" : user.getFirst()) + " " + (user.getLast() == null ? "" : user.getLast()));
        customer.setEmail(user.getEmail());
        customer.setMobile(user.getContact());
        customer.setAddress(address);
        if (customer.getLeadSource() == null || customer.getLeadSource().isBlank()) {
            customer.setLeadSource("Website");
        }
        customer = customerRepository.save(customer);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setCar(car);
        booking.setStatus("Pending");
        booking.setWorkflowStatus("Pending");
        booking.setStatusUpdatedAt(LocalDateTime.now());
        booking.setFullName(fullName);
        booking.setDob(dob);
        booking.setGender(gender);
        booking.setAadhaarNumber(aadhaarNumber);
        booking.setPanNumber(panNumber);
        booking.setAddress(address);
        booking.setCity(city);
        booking.setState(state);
        booking.setPinCode(pinCode);
        booking.setBookingAmount(bookingAmount);
        booking.setPaymentMode(paymentMode);
        booking.setTransactionId(transactionId);
        booking.setPaymentScreenshotUrl(saveFile(paymentScreenshot, "uploads/documents"));
        booking.setExpectedDeliveryDate(expectedDeliveryDate);
        booking.setDeliveryTimeSlot(deliveryTimeSlot);
        booking.setDeliveryType(deliveryType);
        booking.setAadhaarPhotoUrl(saveFile(aadhaarPhoto, "uploads/documents"));
        booking.setPanPhotoUrl(saveFile(panPhoto, "uploads/documents"));
        booking.setSignaturePhotoUrl(saveFile(signaturePhoto, "uploads/documents"));
        booking.setPassportPhotoUrl(saveFile(passportPhoto, "uploads/documents"));

        Booking saved = bookingService.saveBooking(booking);

        Payment payment = new Payment();
        payment.setBooking(saved);
        payment.setAmount(bookingAmount);
        payment.setPaymentMethod(paymentMode);
        payment.setTransactionId(transactionId);
        payment.setStatus("Pending");
        paymentService.savePayment(payment);

        return ResponseEntity.ok(Map.of("ok", true, "bookingId", saved.getId(), "status", saved.getWorkflowStatus()));
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
}
