package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.Sale;
import com.Dk3.Cars.entity.Showroom;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.SaleRepository;
import com.Dk3.Cars.repository.ShowroomRepository;
import com.Dk3.Cars.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.nio.file.*;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpSession;




@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardRestController.class);

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ShowroomRepository showroomRepository;

    @Autowired
    private com.Dk3.Cars.repository.UserRepository userRepository;

    @Autowired
    private com.Dk3.Cars.repository.SettingRepository settingRepository;

    @Autowired
    private CarService carService;

    @Autowired
    private SaleService saleService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TestDriveService testDriveService;

    private boolean isStaffRole(String role) {
        return role != null && !"ROLE_ADMIN".equals(role) && !"ROLE_USER".equals(role);
    }

    private User getSessionUser(HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) return null;
        Long userId = Long.valueOf(String.valueOf(userIdObj));
        return userRepository.findById(userId).orElse(null);
    }

    private boolean canAccessCar(HttpSession session, Car car) {
        String role = (String) session.getAttribute("USER_ROLE");
        if (!isStaffRole(role)) return true;
        User sessionUser = getSessionUser(session);
        Long staffShowroomId = sessionUser == null ? null : sessionUser.getShowroomId();
        Long carShowroomId = car != null && car.getShowroom() != null ? car.getShowroom().getId() : null;
        return staffShowroomId != null && staffShowroomId.equals(carShowroomId);
    }

    private boolean canManageShowroom(HttpSession session, Showroom showroom) {
        String role = (String) session.getAttribute("USER_ROLE");
        if (!isStaffRole(role)) return true;
        User sessionUser = getSessionUser(session);
        Long staffShowroomId = sessionUser == null ? null : sessionUser.getShowroomId();
        Long showroomId = showroom == null ? null : showroom.getId();
        return staffShowroomId != null && staffShowroomId.equals(showroomId);
    }

    private String normalizeColorOptions(String colorOptions) {
        if (colorOptions == null || colorOptions.isBlank()) {
            return null;
        }
        List<String> rows = new ArrayList<>();
        for (String entry : colorOptions.split("[\\r\\n|]+")) {
            String cleaned = entry == null ? "" : entry.trim();
            if (!cleaned.isEmpty()) {
                rows.add(cleaned);
            }
        }
        return rows.isEmpty() ? null : String.join("|", rows);
    }

    private List<String> splitColorOptionColumn(String raw) {
        List<String> rows = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return rows;
        }
        for (String line : raw.split("\\r?\\n")) {
            String cleaned = line == null ? "" : line.trim();
            if (!cleaned.isEmpty()) {
                rows.add(cleaned);
            }
        }
        return rows;
    }

    private String mergeColorOptionColumns(String colorOptionNames,
                                           String colorOptionCodes,
                                           String colorOptionImages,
                                           String fallbackColorOptions) {
        List<String> names = splitColorOptionColumn(colorOptionNames);
        List<String> codes = splitColorOptionColumn(colorOptionCodes);
        List<String> images = splitColorOptionColumn(colorOptionImages);
        int max = Math.max(names.size(), Math.max(codes.size(), images.size()));
        if (max == 0) {
            return normalizeColorOptions(fallbackColorOptions);
        }

        List<String> rows = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            String name = i < names.size() ? names.get(i) : "";
            String code = i < codes.size() ? codes.get(i) : "";
            String image = i < images.size() ? images.get(i) : "";
            if (name.isBlank() && code.isBlank() && image.isBlank()) {
                continue;
            }
            if (name.isBlank()) {
                name = "Color " + (i + 1);
            }
            if (code.isBlank()) {
                code = "#444444";
            }
            rows.add(name + "~" + code + "~" + image);
        }

        return rows.isEmpty() ? normalizeColorOptions(fallbackColorOptions) : String.join("|", rows);
    }

    private List<String> splitFaqColumn(String raw) {
        List<String> rows = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return rows;
        }
        for (String line : raw.split("\\r?\\n")) {
            String cleaned = line == null ? "" : line.trim();
            if (!cleaned.isEmpty()) {
                rows.add(cleaned);
            }
        }
        return rows;
    }

    private String mergeFaqColumns(String faqQuestions, String faqAnswers, String fallbackFaqDetails) {
        List<String> questions = splitFaqColumn(faqQuestions);
        List<String> answers = splitFaqColumn(faqAnswers);
        int max = Math.max(questions.size(), answers.size());
        if (max == 0) {
            return fallbackFaqDetails;
        }

        List<String> rows = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            String question = i < questions.size() ? questions.get(i) : "";
            String answer = i < answers.size() ? answers.get(i) : "";
            if (question.isBlank() && answer.isBlank()) {
                continue;
            }
            if (question.isBlank()) {
                question = "FAQ " + (i + 1);
            }
            rows.add(question + "~" + answer);
        }
        return rows.isEmpty() ? fallbackFaqDetails : String.join("|", rows);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        Map<String, Object> map = new HashMap<>();

        long totalShowroom = showroomRepository.count();
        long soldCars = carRepository.countBySold(true);
        long availableCars = carService.getTotalCarsInStock();
        Double revenue = carRepository.totalRevenue();

        // New dashboard counts
        long totalVehicles = carRepository.count();
        long userRequests = userRepository.countByEnabled(false); // pending accounts
        long activeBookings = bookingService.countBookingsByStatus("Pending") +
                             bookingService.countBookingsByStatus("Confirmed");
        long staffCount = userRepository.countByRoleNot("ROLE_USER");

        // Today's metrics
        long carsSoldToday = saleService.countTodaySales();
        long testDrivesToday = testDriveService.countTodayScheduledTestDrives();

        // Monthly/Yearly revenue
        Double monthlyRevenue = saleService.getMonthlyRevenue(
            java.time.LocalDate.now().getYear(),
            java.time.LocalDate.now().getMonthValue()
        );
        Double yearlyRevenue = saleService.getYearlyRevenue(java.time.LocalDate.now().getYear());

        // Low stock alerts
        List<Object[]> lowStockModels = carService.getLowStockModels();

        map.put("totalShowroom", totalShowroom);
        map.put("soldCars", soldCars);
        map.put("availableCars", availableCars);
        map.put("revenue", revenue == null ? 0 : revenue);

        map.put("totalVehicles", totalVehicles);
        map.put("userRequests", userRequests);
        map.put("activeBookings", activeBookings);
        map.put("staffCount", staffCount);

        map.put("carsSoldToday", carsSoldToday);
        map.put("carsSoldThisMonth", saleService.countCurrentMonthSales());
        map.put("carsSoldThisYear", saleService.countYearlySales(java.time.LocalDate.now().getYear()));
        map.put("testDrivesToday", testDrivesToday);
        map.put("monthlyRevenue", monthlyRevenue == null ? 0 : monthlyRevenue);
        map.put("yearlyRevenue", yearlyRevenue == null ? 0 : yearlyRevenue);
        map.put("lowStockAlerts", lowStockModels);

        return map;
    }



    @GetMapping("/sales")
    public List<Map<String, Object>> getRecentSales() {

        return saleRepository.findTop5ByOrderBySoldDateDesc()
                .stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("model", s.getCar().getModel());
                    m.put("buyer", s.getBuyerName());
                    m.put("date", s.getSoldDate());
                    m.put("status", s.getStatus());
                    return m;
                })
                .toList();
    }


    @GetMapping("/top-cars")
    public List<String> getTopCars() {

        return carRepository.findAll(PageRequest.of(0,4))
                .stream()
                .map(Car::getModel)
                .toList();
    }

    @GetMapping("/inventory")
    public List<Map<String, Object>> getInventoryOverview() {

        return carRepository.countCarsByBrand()
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("brand", row[0]);
                    map.put("count", row[1]);
                    return map;
                })
                .toList();
    }


    @GetMapping("/top-selling")
    public List<Map<String, Object>> getTopSellingCars() {

        return carRepository
                .topSellingCars(PageRequest.of(0, 5))   // ✅ FIXED SYNTAX
                .stream()
                .map(row -> {
                    Object[] data = (Object[]) row;

                    Map<String, Object> map = new HashMap<>();
                    map.put("model", data[0]);
                    map.put("totalSold", data[1]);
                    return map;
                })
                .toList();
    }

    @GetMapping("/showrooms")
    public List<Map<String, Object>> getShowrooms() {

        return showroomRepository.findAll()
                .stream()
                .map(this::showroomToMap)
                .toList();
    }

    @GetMapping("/showroom/{id}")
    public Map<String, Object> getShowroom(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        Showroom showroom = showroomRepository.findById(id).orElse(null);
        if (showroom == null) {
            response.put("ok", false);
            response.put("message", "Showroom not found.");
            return response;
        }
        if (!canManageShowroom(session, showroom)) {
            response.put("ok", false);
            response.put("message", "You do not have permission to view this showroom.");
            return response;
        }
        response.put("ok", true);
        response.put("showroom", showroomToMap(showroom));
        return response;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentStaff(HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        User user = getSessionUser(session);
        if (user == null) {
            resp.put("ok", false);
            resp.put("message", "User not found");
            return resp;
        }

        resp.put("ok", true);
        resp.put("userid", user.getUserid());
        resp.put("first", user.getFirst());
        resp.put("last", user.getLast());
        resp.put("email", user.getEmail());
        resp.put("contact", user.getContact());
        resp.put("role", user.getRole());
        resp.put("profilePhotoUrl", user.getProfilePhotoUrl());
        resp.put("showroomId", user.getShowroomId());
        String showroomName = user.getShowroomId() == null ? null :
                showroomRepository.findById(user.getShowroomId()).map(Showroom::getName).orElse(null);
        resp.put("showroomName", showroomName);
        return resp;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        Map<String, Object> map = new HashMap<>();
        long bookingsToday = 0;
        try {
            bookingsToday = saleRepository.countBySoldDate(java.time.LocalDate.now());
        } catch (Exception ex) {
            logger.warn("Could not read bookings today", ex);
        }
        long vehiclesAvailable = carRepository.countBySold(false);
        long pendingReviews = 0;
        try {
            pendingReviews = saleRepository.countByStatus("PendingReview");
        } catch (Exception ex) {
            // default to 0
        }
        map.put("bookingsToday", bookingsToday);
        map.put("vehiclesAvailable", vehiclesAvailable);
        map.put("pendingReviews", pendingReviews);
        return map;
    }

    // New Chart Endpoints
    @GetMapping("/charts/monthly-sales")
    public List<Map<String, Object>> getMonthlySalesData() {
        // This would return data for monthly sales bar chart
        // For now, return sample data - in real implementation, query database
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> month = new HashMap<>();
            month.put("month", java.time.Month.of(i).name());
            month.put("sales", Math.random() * 50 + 10); // Sample data
            data.add(month);
        }
        return data;
    }

    @GetMapping("/charts/model-sales")
    public List<Map<String, Object>> getModelSalesData() {
        return carRepository.topSellingCars(PageRequest.of(0, 10))
                .stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("model", row[0]);
                    map.put("count", row[1]);
                    return map;
                })
                .toList();
    }

    @GetMapping("/charts/revenue-trend")
    public List<Map<String, Object>> getRevenueTrendData() {
        // Sample revenue trend data - in real implementation, query by months
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> month = new HashMap<>();
            month.put("month", java.time.Month.of(i).name().substring(0, 3));
            month.put("revenue", Math.random() * 100000 + 50000); // Sample data
            data.add(month);
        }
        return data;
    }

    @GetMapping("/staff")
    public List<Map<String, Object>> getStaff() {
        return userRepository.findByRole("ROLE_STAFF")
                .stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userid", u.getUserid());
                    m.put("first", u.getFirst());
                    m.put("last", u.getLast());
                    m.put("email", u.getEmail());
                    m.put("contact", u.getContact());
                    m.put("showroomId", u.getShowroomId());
                    String showroomName = u.getShowroomId() == null ? null :
                            showroomRepository.findById(u.getShowroomId()).map(Showroom::getName).orElse(null);
                    m.put("showroomName", showroomName);
                    return m;
                }).toList();
    }

    @DeleteMapping("/staff/{id}")
    public Map<String, Object> deleteStaff(@PathVariable Long id) {
        userRepository.deleteById(id);
        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        return resp;
    }

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/staff")
    public Map<String, Object> createStaff(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        Map<String, Object> resp = new HashMap<>();
        if (email == null || email.trim().isEmpty()) {
            resp.put("ok", false);
            resp.put("message", "Email is required");
            return resp;
        }
        if (userRepository.existsByEmail(email)) {
            resp.put("ok", false);
            resp.put("message", "Email already exists");
            return resp;
        }

        String first = (String) body.getOrDefault("first", "");
        String last = (String) body.getOrDefault("last", "");
        String contact = normalizeContact((String) body.getOrDefault("contact", ""));
        String pwd = (String) body.getOrDefault("password", "changeme123");
        String role = (String) body.getOrDefault("role", "ROLE_STAFF");
        boolean enabled = Boolean.parseBoolean(String.valueOf(body.getOrDefault("enabled", true)));
        Long showroomId = body.get("showroomId") == null || String.valueOf(body.get("showroomId")).isBlank()
                ? null : Long.valueOf(String.valueOf(body.get("showroomId")));
        if (showroomId != null && showroomRepository.findById(showroomId).isEmpty()) {
            resp.put("ok", false);
            resp.put("message", "Invalid showroomId");
            return resp;
        }
        if (contact != null && !contact.isBlank() && !contact.matches("\\d{10}")) {
            resp.put("ok", false);
            resp.put("message", "Mobile number must be exactly 10 digits");
            return resp;
        }

        com.Dk3.Cars.entity.User u = new com.Dk3.Cars.entity.User();
        u.setFirst(first);
        u.setLast(last);
        u.setEmail(email);
        u.setContact(contact);
        u.setPassword(passwordEncoder.encode(pwd));
        u.setRole(role);
        u.setEnabled(enabled);
        u.setShowroomId(showroomId);

        userRepository.save(u);
        resp.put("ok", true);
        resp.put("userid", u.getUserid());
        return resp;
    }

    @PutMapping("/staff/{id}")
    public Map<String, Object> updateStaff(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        com.Dk3.Cars.entity.User u = userRepository.findById(id).orElse(null);
        if (u == null) {
            resp.put("ok", false);
            resp.put("message", "User not found");
            return resp;
        }

        if (body.containsKey("first")) u.setFirst((String) body.get("first"));
        if (body.containsKey("last")) u.setLast((String) body.get("last"));
        if (body.containsKey("contact")) {
            String contact = normalizeContact((String) body.get("contact"));
            if (contact != null && !contact.isBlank() && !contact.matches("\\d{10}")) {
                resp.put("ok", false);
                resp.put("message", "Mobile number must be exactly 10 digits");
                return resp;
            }
            u.setContact(contact);
        }
        if (body.containsKey("role")) u.setRole((String) body.get("role"));
        if (body.containsKey("enabled")) u.setEnabled(Boolean.parseBoolean(String.valueOf(body.get("enabled"))));
        if (body.containsKey("showroomId")) {
            String showroomVal = String.valueOf(body.get("showroomId"));
            if (showroomVal == null || showroomVal.isBlank() || "null".equalsIgnoreCase(showroomVal)) {
                u.setShowroomId(null);
            } else {
                Long parsedShowroomId = Long.valueOf(showroomVal);
                if (showroomRepository.findById(parsedShowroomId).isEmpty()) {
                    resp.put("ok", false);
                    resp.put("message", "Invalid showroomId");
                    return resp;
                }
                u.setShowroomId(parsedShowroomId);
            }
        }
        if (body.containsKey("password")) {
            String pwd = (String) body.get("password");
            if (pwd != null && !pwd.trim().isEmpty()) u.setPassword(passwordEncoder.encode(pwd));
        }

        userRepository.save(u);
        resp.put("ok", true);
        return resp;
    }

    @PostMapping("/showroom")
    public Map<String, Object> saveShowroom(
            @RequestParam String name,
            @RequestParam String city,
            @RequestParam String address,
            @RequestParam String contactNumber,
            @RequestParam String type,
            @RequestParam String workingHours,
            @RequestParam(required = false) MultipartFile image) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();

        String showroomName = trimToNull(name);
        String showroomCity = trimToNull(city);
        String showroomAddress = trimToNull(address);
        String showroomContact = trimToNull(contactNumber);
        String showroomType = trimToNull(type);
        String showroomWorkingHours = trimToNull(workingHours);

        if (showroomName == null || showroomCity == null || showroomAddress == null
                || showroomContact == null || showroomWorkingHours == null) {
            response.put("ok", false);
            response.put("message", "Name, city, address, contact number, and working details are required.");
            return response;
        }

        String digits = showroomContact.replaceAll("\\D", "");
        if (digits.length() != 10) {
            response.put("ok", false);
            response.put("message", "Contact number must be exactly 10 digits.");
            return response;
        }

        Showroom showroom = new Showroom();
        showroom.setName(showroomName);
        showroom.setCity(showroomCity);
        showroom.setAddress(showroomAddress);
        showroom.setContactNumber(digits);
        showroom.setType(showroomType == null ? "Normal" : showroomType);
        showroom.setWorkingHours(showroomWorkingHours);
        showroom.setMapUrl(null);
        showroom.setImageUrl(storeShowroomImage(image));

        Showroom saved = showroomRepository.save(showroom);
        response.put("ok", true);
        response.put("message", "Showroom added successfully.");
        response.put("showroom", showroomToMap(saved));
        return response;
    }

    @PutMapping("/showroom/{id}")
    public Map<String, Object> updateShowroom(@PathVariable Long id,
                                              @RequestBody Map<String, Object> body,
                                              HttpSession session) {

        Map<String, Object> response = new LinkedHashMap<>();
        Showroom showroom = showroomRepository.findById(id).orElse(null);
        if (showroom == null) {
            response.put("ok", false);
            response.put("message", "Showroom not found.");
            return response;
        }
        if (!canManageShowroom(session, showroom)) {
            response.put("ok", false);
            response.put("message", "You do not have permission to edit this showroom.");
            return response;
        }

        String showroomName = trimToNull(body.containsKey("name") ? String.valueOf(body.get("name")) : showroom.getName());
        String showroomCity = trimToNull(body.containsKey("city") ? String.valueOf(body.get("city")) : showroom.getCity());
        String showroomAddress = trimToNull(body.containsKey("address") ? String.valueOf(body.get("address")) : showroom.getAddress());
        String showroomContact = trimToNull(body.containsKey("contactNumber") ? String.valueOf(body.get("contactNumber")) : showroom.getContactNumber());
        String showroomType = trimToNull(body.containsKey("type") ? String.valueOf(body.get("type")) : showroom.getType());
        String showroomWorkingHours = trimToNull(body.containsKey("workingHours") ? String.valueOf(body.get("workingHours")) : showroom.getWorkingHours());
        String showroomImageUrl = trimToNull(body.containsKey("imageUrl") ? String.valueOf(body.get("imageUrl")) : showroom.getImageUrl());

        if (showroomName == null || showroomCity == null || showroomAddress == null
                || showroomContact == null || showroomWorkingHours == null) {
            response.put("ok", false);
            response.put("message", "Name, city, address, contact number, and working details are required.");
            return response;
        }

        String digits = showroomContact.replaceAll("\\D", "");
        if (digits.length() != 10) {
            response.put("ok", false);
            response.put("message", "Contact number must be exactly 10 digits.");
            return response;
        }

        showroom.setName(showroomName);
        showroom.setCity(showroomCity);
        showroom.setAddress(showroomAddress);
        showroom.setContactNumber(digits);
        showroom.setType(showroomType == null ? "Normal" : showroomType);
        showroom.setWorkingHours(showroomWorkingHours);
        if (showroomImageUrl != null) {
            showroom.setImageUrl(showroomImageUrl);
        }

        Showroom saved = showroomRepository.save(showroom);
        response.put("ok", true);
        response.put("message", "Showroom updated successfully.");
        response.put("showroom", showroomToMap(saved));
        return response;
    }

    @PostMapping("/showroom/{id}")
    public Map<String, Object> updateShowroomForm(@PathVariable Long id,
                                                  @RequestParam String name,
                                                  @RequestParam String city,
                                                  @RequestParam String address,
                                                  @RequestParam String contactNumber,
                                                  @RequestParam String type,
                                                  @RequestParam String workingHours,
                                                  @RequestParam(required = false) MultipartFile image,
                                                  HttpSession session) throws Exception {

        Map<String, Object> response = new LinkedHashMap<>();
        Showroom showroom = showroomRepository.findById(id).orElse(null);
        if (showroom == null) {
            response.put("ok", false);
            response.put("message", "Showroom not found.");
            return response;
        }
        if (!canManageShowroom(session, showroom)) {
            response.put("ok", false);
            response.put("message", "You do not have permission to edit this showroom.");
            return response;
        }

        String showroomName = trimToNull(name);
        String showroomCity = trimToNull(city);
        String showroomAddress = trimToNull(address);
        String showroomContact = trimToNull(contactNumber);
        String showroomType = trimToNull(type);
        String showroomWorkingHours = trimToNull(workingHours);

        if (showroomName == null || showroomCity == null || showroomAddress == null
                || showroomContact == null || showroomWorkingHours == null) {
            response.put("ok", false);
            response.put("message", "Name, city, address, contact number, and working details are required.");
            return response;
        }

        String digits = showroomContact.replaceAll("\\D", "");
        if (digits.length() != 10) {
            response.put("ok", false);
            response.put("message", "Contact number must be exactly 10 digits.");
            return response;
        }

        showroom.setName(showroomName);
        showroom.setCity(showroomCity);
        showroom.setAddress(showroomAddress);
        showroom.setContactNumber(digits);
        showroom.setType(showroomType == null ? "Normal" : showroomType);
        showroom.setWorkingHours(showroomWorkingHours);
        if (image != null && !image.isEmpty()) {
            showroom.setImageUrl(storeShowroomImage(image));
        }

        Showroom saved = showroomRepository.save(showroom);
        response.put("ok", true);
        response.put("message", "Showroom updated successfully.");
        response.put("showroom", showroomToMap(saved));
        return response;
    }

    @DeleteMapping("/showroom/{id}")
    public Map<String, Object> deleteShowroom(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new LinkedHashMap<>();
        Showroom showroom = showroomRepository.findById(id).orElse(null);
        if (showroom == null) {
            response.put("ok", false);
            response.put("message", "Showroom not found.");
            return response;
        }
        if (!canManageShowroom(session, showroom)) {
            response.put("ok", false);
            response.put("message", "You do not have permission to delete this showroom.");
            return response;
        }

        long linkedCars = carRepository.countByShowroomId(id);
        if (linkedCars > 0) {
            response.put("ok", false);
            response.put("message", "This showroom has cars linked to it. Remove those cars first.");
            return response;
        }

        long linkedUsers = userRepository.countByShowroomId(id);
        if (linkedUsers > 0) {
            response.put("ok", false);
            response.put("message", "This showroom is assigned to staff/admin accounts. Remove those assignments first.");
            return response;
        }

        showroomRepository.delete(showroom);
        response.put("ok", true);
        response.put("message", "Showroom deleted successfully.");
        return response;
    }

    private String normalizeContact(String value) {
        if (value == null) {
            return "";
        }
        String digits = value.replaceAll("\\D", "");
        return digits.length() > 10 ? digits.substring(0, 10) : digits;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String storeShowroomImage(MultipartFile image) throws Exception {
        if (image == null || image.isEmpty()) {
            return "/images/background.jpg";
        }

        String originalName = image.getOriginalFilename() == null ? "showroom.jpg" : image.getOriginalFilename();
        String sanitizedName = Paths.get(originalName).getFileName().toString().replaceAll("\\s+", "-");
        String fileName = UUID.randomUUID() + "_" + sanitizedName;
        Path uploadDir = Paths.get("uploads");
        Files.createDirectories(uploadDir);
        Path path = uploadDir.resolve(fileName);
        Files.write(path, image.getBytes());
        return "/uploads/" + fileName;
    }

    private Map<String, Object> showroomToMap(Showroom showroom) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", showroom.getId());
        map.put("name", showroom.getName());
        map.put("city", showroom.getCity());
        map.put("address", showroom.getAddress());
        map.put("contactNumber", showroom.getContactNumber());
        map.put("type", showroom.getType());
        map.put("workingHours", showroom.getWorkingHours());
        map.put("mapUrl", showroom.getMapUrl());
        map.put("managerName", showroom.getManagerName());
        map.put("image", showroom.getImageUrl());
        map.put("imageUrl", showroom.getImageUrl());
        return map;
    }

    @GetMapping("/showrooms/{id}/cars")
    public java.util.List<java.util.Map<String, Object>> getCarsByShowroom(@PathVariable Long id, HttpSession session) {
        Long targetShowroomId = id;
        String role = (String) session.getAttribute("USER_ROLE");
        if (isStaffRole(role)) {
            User sessionUser = getSessionUser(session);
            if (sessionUser == null || sessionUser.getShowroomId() == null) {
                return java.util.Collections.emptyList();
            }
            if (!sessionUser.getShowroomId().equals(id)) {
                return java.util.Collections.emptyList();
            }
            targetShowroomId = sessionUser.getShowroomId();
        }

        return carRepository.findByShowroomIdOrderByIdDesc(targetShowroomId)
                .stream()
                .map(c -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", c.getId());
                    m.put("model", c.getModel());
                    m.put("brand", c.getBrand());
                    m.put("price", c.getPrice());
                    m.put("available", c.isAvailable());
                    m.put("image", c.getImageUrls());
                    // placeholder for description or other fields
                    m.put("description", "");
                    return m;
                }).toList();
    }

    @PostMapping("/cars/{id}/sell")
    public java.util.Map<String, Object> sellCar(@PathVariable Long id, HttpSession session) {
        Car car = carRepository.findById(id).orElseThrow();
        if (!canAccessCar(session, car)) {
            java.util.Map<String, Object> denied = new java.util.HashMap<>();
            denied.put("ok", false);
            denied.put("message", "Access denied for this vehicle");
            return denied;
        }
        car.setSold(true);
        carRepository.save(car);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("ok", true);
        return resp;
    }

        @PostMapping("/cars/add")
        public java.util.Map<String, Object> addCar(
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam(required = false) String variant,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String mileage,
            @RequestParam Double price,
            @RequestParam(required = false) String engineCc,
            @RequestParam(required = false) String safetyRating,
            @RequestParam(required = false) String seatingCapacity,
            @RequestParam(required = false) String fuelOptions,
            @RequestParam(required = false) String transmissionOptions,
            @RequestParam(required = false) String mileageDetails,
            @RequestParam(required = false) String variantDetails,
            @RequestParam(required = false) String colorOptions,
            @RequestParam(required = false) String colorOptionNames,
            @RequestParam(required = false) String colorOptionCodes,
            @RequestParam(required = false) String colorOptionImages,
            @RequestParam(required = false) Double reviewScore,
            @RequestParam(required = false) Double reviewExterior,
            @RequestParam(required = false) Double reviewPerformance,
            @RequestParam(required = false) Double reviewValue,
            @RequestParam(required = false) Double reviewFuelEconomy,
            @RequestParam(required = false) Double reviewComfort,
            @RequestParam(required = false) String faqQuestions,
            @RequestParam(required = false) String faqAnswers,
            @RequestParam(required = false) String faqDetails,
            @RequestParam(required = false) String vin,
            @RequestParam(required = false) String engineNo,
            @RequestParam(required = false) String purchaseDate,
            @RequestParam(required = false) String supplierInfo,
            @RequestParam(required = false) Long showroom,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer stockQuantity,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session) {

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        String role = (String) session.getAttribute("USER_ROLE");
        Long effectiveShowroomId = showroom;

        if (isStaffRole(role)) {
            User sessionUser = getSessionUser(session);
            if (sessionUser == null || sessionUser.getShowroomId() == null) {
                response.put("success", false);
                response.put("message", "No showroom allocated to this staff account.");
                return response;
            }
            effectiveShowroomId = sessionUser.getShowroomId();
        }

        if (effectiveShowroomId == null) {
            response.put("success", false);
            response.put("message", "Showroom is required.");
            return response;
        }

        Car car = new Car();
        car.setBrand(brand);
        car.setModel(model);
        car.setVariant(variant);
        car.setColor(color);
        car.setFuelType(fuelType);
        car.setTransmission(transmission);
        car.setMileage(mileage);
        car.setPrice(price);
        car.setEngineCc(engineCc);
        car.setSafetyRating(safetyRating);
        car.setSeatingCapacity(seatingCapacity);
        car.setFuelOptions(fuelOptions);
        car.setTransmissionOptions(transmissionOptions);
        car.setMileageDetails(mileageDetails);
        car.setVariantDetails(variantDetails);
        car.setColorOptions(mergeColorOptionColumns(colorOptionNames, colorOptionCodes, colorOptionImages, colorOptions));
        car.setReviewScore(reviewScore);
        car.setReviewExterior(reviewExterior);
        car.setReviewPerformance(reviewPerformance);
        car.setReviewValue(reviewValue);
        car.setReviewFuelEconomy(reviewFuelEconomy);
        car.setReviewComfort(reviewComfort);
        car.setFaqDetails(mergeFaqColumns(faqQuestions, faqAnswers, faqDetails));
        car.setVin(vin);
        car.setEngineNo(engineNo);
        car.setSupplierInfo(supplierInfo);
        car.setStatus(status != null ? status : "Available");
        car.setStockQuantity(stockQuantity);

        Showroom sr = showroomRepository.findById(effectiveShowroomId).orElse(null);
        car.setShowroom(sr);

        if (purchaseDate != null && !purchaseDate.isEmpty()) {
            try {
                java.time.LocalDate date = java.time.LocalDate.parse(purchaseDate);
                car.setPurchaseDate(date);
            } catch (Exception e) {
                logger.warn("Could not parse purchase date: " + purchaseDate);
            }
        }

        Car savedCar = carRepository.save(car);

        java.util.List<String> urls = new java.util.ArrayList<>();

        if (imageUrl != null && !imageUrl.isBlank()) {
            String cleanedUrl = imageUrl.trim();
            if (!cleanedUrl.isEmpty()) {
                urls.add(cleanedUrl);
            }
        }

        if (image != null && !image.isEmpty()) {
            try {
                String uploadDir = "uploads/cars/";
                Files.createDirectories(Paths.get(uploadDir));
                String original = image.getOriginalFilename();
                String fileName = UUID.randomUUID().toString() + "_" + (original == null ? "img" : original.replaceAll("\\s+", "_"));
                Path p = Paths.get(uploadDir + fileName);
                Files.write(p, image.getBytes());
                urls.add("/uploads/cars/" + fileName);
            } catch (Exception ex) {
                logger.warn("Failed to save uploaded image", ex);
            }
        }

        if (!urls.isEmpty()) {
            savedCar.setImageUrls(urls);
            carRepository.save(savedCar);
        }

        response.put("success", true);
        response.put("message", "Vehicle added successfully!");
        response.put("carId", savedCar.getId());
        response.put("redirectUrl", "/cars/showroom/" + effectiveShowroomId);
        return response;
    }

    @GetMapping("/cars")
    public java.util.List<java.util.Map<String, Object>> getAllCars(HttpSession session) {
        String role = (String) session.getAttribute("USER_ROLE");
        java.util.List<Car> cars;

        if (isStaffRole(role)) {
            User sessionUser = getSessionUser(session);
            if (sessionUser == null || sessionUser.getShowroomId() == null) {
                return java.util.Collections.emptyList();
            }
            cars = carRepository.findByShowroomIdOrderByIdDesc(sessionUser.getShowroomId());
        } else {
            cars = carRepository.findAll();
        }

        return cars
                .stream()
                .map(c -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", c.getId());
                    m.put("model", c.getModel());
                    m.put("brand", c.getBrand());
                    m.put("price", c.getPrice());
                    m.put("available", c.isAvailable());
                    m.put("image", c.getImageUrls());
                    m.put("showroomId", c.getShowroom() != null ? c.getShowroom().getId() : null);
                    return m;
                }).toList();
    }

    @GetMapping("/sales/all")
    public java.util.List<java.util.Map<String, Object>> getAllSales() {
        return saleRepository.findAll()
                .stream()
                .map(s -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", s.getId());
                    m.put("buyer", s.getBuyerName());
                    m.put("date", s.getSoldDate());
                    m.put("status", s.getStatus());
                    Car c = s.getCar();
                    if (c != null) {
                        m.put("carId", c.getId());
                        m.put("model", c.getModel());
                        m.put("brand", c.getBrand());
                    }
                    return m;
                }).toList();
    }

    @GetMapping("/sales/{id}")
    public java.util.Map<String, Object> getSale(@PathVariable Long id) {
        Sale s = saleRepository.findById(id).orElseThrow();
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", s.getId());
        m.put("buyer", s.getBuyerName());
        m.put("date", s.getSoldDate());
        m.put("status", s.getStatus());
        Car c = s.getCar();
        if (c != null) {
            m.put("carId", c.getId());
            m.put("model", c.getModel());
            m.put("brand", c.getBrand());
            m.put("price", c.getPrice());
            m.put("image", c.getImageUrls());
        }
        return m;
    }

    @PostMapping("/sales")
    public java.util.Map<String, Object> createSale(@RequestBody java.util.Map<String, Object> body) {
        Long carId = body.get("carId") == null ? null : Long.valueOf(String.valueOf(body.get("carId")));
        String buyer = (String) body.getOrDefault("buyer", "");
        String status = (String) body.getOrDefault("status", "Paid");

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        if (carId == null) {
            resp.put("ok", false);
            resp.put("message", "carId is required");
            return resp;
        }
        Car car = carRepository.findById(carId).orElse(null);
        if (car == null) {
            resp.put("ok", false);
            resp.put("message", "Car not found");
            return resp;
        }

        Sale s = new Sale();
        s.setCar(car);
        s.setBuyerName(buyer);
        s.setStatus(status);
        s.setSoldDate(java.time.LocalDate.now());
        Sale saved = saleRepository.save(s);

        // mark car sold if status indicates paid/completed
        if ("Paid".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            car.setSold(true);
            carRepository.save(car);
        }

        resp.put("ok", true);
        resp.put("id", saved.getId());
        return resp;
    }

    @PutMapping("/sales/{id}")
    public java.util.Map<String, Object> updateSale(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        Sale s = saleRepository.findById(id).orElse(null);
        if (s == null) {
            resp.put("ok", false);
            resp.put("message", "Sale not found");
            return resp;
        }
        if (body.containsKey("status")) {
            String status = (String) body.get("status");
            s.setStatus(status);
            if ("Paid".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
                Car c = s.getCar(); if (c!=null){ c.setSold(true); carRepository.save(c); }
            }
        }
        if (body.containsKey("buyer")) s.setBuyerName((String) body.get("buyer"));
        saleRepository.save(s);
        resp.put("ok", true);
        return resp;
    }

    @DeleteMapping("/sales/{id}")
    public java.util.Map<String, Object> deleteSale(@PathVariable Long id){
        Sale s = saleRepository.findById(id).orElse(null);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        if(s==null){ resp.put("ok", false); resp.put("message","Sale not found"); return resp; }
        Car c = s.getCar();
        saleRepository.deleteById(id);
        if(c!=null){ c.setSold(false); carRepository.save(c); }
        resp.put("ok", true);
        return resp;
    }

    // ---------------- REPORTS -----------------

    @GetMapping("/reports/summary")
    public java.util.Map<String, Object> getReportsSummary() {
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        long totalSales = saleRepository.count();
        Double revenue = carRepository.totalRevenue();
        long totalVehicles = carRepository.count();
        long soldCars = carRepository.countBySold(true);
        long availableCars = carRepository.countBySold(false);
        long staffCount = userRepository.countByRoleNot("ROLE_USER");

        m.put("totalSales", totalSales);
        m.put("revenue", revenue == null ? 0 : revenue);
        m.put("totalVehicles", totalVehicles);
        m.put("soldCars", soldCars);
        m.put("availableCars", availableCars);
        m.put("staffCount", staffCount);
        return m;
    }

    @GetMapping("/reports/sales")
    public java.util.List<java.util.Map<String, Object>> getSalesInRange(@RequestParam String from, @RequestParam String to) {
        java.time.LocalDate f = java.time.LocalDate.parse(from);
        java.time.LocalDate t = java.time.LocalDate.parse(to);
        return saleRepository.findAll()
                .stream()
                .filter(s -> s.getSoldDate() != null && ( !s.getSoldDate().isBefore(f) && !s.getSoldDate().isAfter(t) ))
                .map(s -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", s.getId());
                    m.put("buyer", s.getBuyerName());
                    m.put("date", s.getSoldDate());
                    m.put("status", s.getStatus());
                    Car c = s.getCar();
                    if(c!=null){ m.put("carId", c.getId()); m.put("model", c.getModel()); m.put("brand", c.getBrand()); m.put("price", c.getPrice()); }
                    return m;
                }).toList();
    }

    @GetMapping("/reports/revenue-by-year")
    public java.util.List<java.util.Map<String, Object>> getRevenueByYear(@RequestParam int year) {
        double[] months = new double[12];
        saleRepository.findAll().forEach(s -> {
            if (s.getSoldDate() == null) return;
            if (s.getSoldDate().getYear() != year) return;
            String status = s.getStatus() == null ? "" : s.getStatus();
            if ("Paid".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
                Car c = s.getCar();
                if (c != null) {
                    int m = s.getSoldDate().getMonthValue() - 1;
                    months[m] += c.getPrice();
                }
            }
        });
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (int i = 0; i < 12; i++) {
            java.util.Map<String, Object> mm = new java.util.HashMap<>();
            mm.put("month", i + 1);
            mm.put("revenue", months[i]);
            out.add(mm);
        }
        return out;
    }

    @GetMapping("/reports/top-brands")
    public java.util.List<java.util.Map<String, Object>> getTopBrands(@RequestParam(defaultValue = "10") int limit) {
        java.util.Map<String, Long> counts = new java.util.HashMap<>();
        saleRepository.findAll().forEach(s -> {
            String status = s.getStatus() == null ? "" : s.getStatus();
            if ("Paid".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
                Car c = s.getCar();
                if (c != null) counts.put(c.getBrand(), counts.getOrDefault(c.getBrand(), 0L) + 1);
            }
        });
        return counts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("brand", e.getKey());
                    m.put("sold", e.getValue());
                    return m;
                }).toList();
    }

    // ---------------- SETTINGS -----------------

    @GetMapping("/settings")
    public java.util.Map<String, String> getSettings() {
        java.util.Map<String, String> out = new java.util.HashMap<>();
        // provide some sensible defaults if not present
        java.util.Map<String, String> defaults = java.util.Map.of(
                "siteTitle", "DK3 Cars",
                "contactEmail", "support@dk3cars.example",
                "currency", "INR",
                "maintenanceMode", "false"
        );

        settingRepository.findAll().forEach(s -> out.put(s.getName(), s.getValue()));
        defaults.forEach((k, v) -> out.putIfAbsent(k, v));
        return out;
    }

    @PutMapping("/settings")
    public java.util.Map<String, Object> updateSettings(@RequestBody java.util.Map<String, Object> body) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        body.forEach((k, v) -> {
            String val = v == null ? "" : String.valueOf(v);
            var opt = settingRepository.findByName(k);
            com.Dk3.Cars.entity.Setting s;
            if (opt.isPresent()) {
                s = opt.get();
                s.setValue(val);
            } else {
                s = new com.Dk3.Cars.entity.Setting();
                s.setName(k);
                s.setValue(val);
            }
            settingRepository.save(s);
        });
        resp.put("ok", true);
        resp.put("saved", body.keySet());
        return resp;
    }

    @DeleteMapping("/settings/{name}")
    public java.util.Map<String, Object> deleteSetting(@PathVariable String name) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        var opt = settingRepository.findByName(name);
        if (opt.isPresent()) {
            settingRepository.delete(opt.get());
            resp.put("ok", true);
            resp.put("deleted", name);
        } else {
            resp.put("ok", false);
            resp.put("error", "not found");
        }
        return resp;
    }

    // more granular CRUD
    @GetMapping("/settings/list")
    public java.util.List<com.Dk3.Cars.entity.Setting> listSettings() {
        return settingRepository.findAll();
    }

    @PostMapping("/settings")
    public java.util.Map<String, Object> createSetting(@RequestBody java.util.Map<String, String> body) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        String name = body.getOrDefault("name", "").trim();
        String value = body.getOrDefault("value", "");
        if (name.isEmpty()) {
            resp.put("ok", false);
            resp.put("error", "name required");
            return resp;
        }
        if (settingRepository.findByName(name).isPresent()) {
            resp.put("ok", false);
            resp.put("error", "exists");
            return resp;
        }
        com.Dk3.Cars.entity.Setting s = new com.Dk3.Cars.entity.Setting();
        s.setName(name);
        s.setValue(value);
        settingRepository.save(s);
        resp.put("ok", true);
        resp.put("setting", s);
        return resp;
    }

    @PutMapping("/settings/{id}")
    public java.util.Map<String, Object> updateSettingById(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        var opt = settingRepository.findById(id);
        if (opt.isEmpty()) {
            resp.put("ok", false);
            resp.put("error", "not found");
            return resp;
        }
        com.Dk3.Cars.entity.Setting s = opt.get();
        String newName = body.getOrDefault("name", s.getName()).trim();
        String newVal = body.getOrDefault("value", s.getValue());
        if (!newName.equals(s.getName()) && settingRepository.findByName(newName).isPresent()) {
            resp.put("ok", false);
            resp.put("error", "name exists");
            return resp;
        }
        s.setName(newName);
        s.setValue(newVal);
        settingRepository.save(s);
        resp.put("ok", true);
        resp.put("setting", s);
        return resp;
    }

    @DeleteMapping("/settings/id/{id}")
    public java.util.Map<String, Object> deleteSettingById(@PathVariable Long id) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        var opt = settingRepository.findById(id);
        if (opt.isPresent()) {
            settingRepository.delete(opt.get());
            resp.put("ok", true);
            resp.put("deletedId", id);
        } else {
            resp.put("ok", false);
            resp.put("error", "not found");
        }
        return resp;
    }

    // ===== CAR MANAGEMENT API ENDPOINTS =====



    @GetMapping("/cars/{id}")
    public Map<String, Object> getCarById(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Car car = carRepository.findById(id).orElse(null);

            if (car == null) {
                response.put("success", false);
                response.put("message", "Car not found");
                return response;
            }

            if (!canAccessCar(session, car)) {
                response.put("success", false);
                response.put("message", "Access denied for this vehicle");
                return response;
            }

            response.put("success", true);
            response.put("car", car);
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error retrieving car: " + e.getMessage());
            return response;
        }
    }



    @DeleteMapping("/cars/{id}")
    public Map<String, Object> deleteCar(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Car car = carRepository.findById(id).orElse(null);

            if (car == null) {
                response.put("success", false);
                response.put("message", "Car not found");
                return response;
            }

            if (!canAccessCar(session, car)) {
                response.put("success", false);
                response.put("message", "Access denied for this vehicle");
                return response;
            }

            carService.deleteCar(id);

            response.put("success", true);
            response.put("message", "Vehicle deleted successfully!");
            response.put("deletedId", id);
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting car: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/cars/{id}/mark-sold")
    public Map<String, Object> markCarSold(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Car car = carRepository.findById(id).orElse(null);

            if (car == null) {
                response.put("success", false);
                response.put("message", "Car not found");
                return response;
            }

            if (!canAccessCar(session, car)) {
                response.put("success", false);
                response.put("message", "Access denied for this vehicle");
                return response;
            }

            car.setStatus("Sold");
            car.setSold(true);
            carService.saveCar(car);

            response.put("success", true);
            response.put("message", "Vehicle marked as sold!");
            response.put("car", car);
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking car as sold: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/cars/{id}/mark-available")
    public Map<String, Object> markCarAvailable(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Car car = carRepository.findById(id).orElse(null);

            if (car == null) {
                response.put("success", false);
                response.put("message", "Car not found");
                return response;
            }

            if (!canAccessCar(session, car)) {
                response.put("success", false);
                response.put("message", "Access denied for this vehicle");
                return response;
            }

            car.setStatus("Available");
            car.setSold(false);
            carService.saveCar(car);

            response.put("success", true);
            response.put("message", "Vehicle marked as available!");
            response.put("car", car);
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking car as available: " + e.getMessage());
            return response;
        }
    }

    @GetMapping("/cars/search")
    public Map<String, Object> searchCars(@RequestParam(value = "keyword") String keyword) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Car> cars = carService.searchCars(keyword);

            response.put("success", true);
            response.put("total", cars.size());
            response.put("cars", cars);
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error searching cars: " + e.getMessage());
            return response;
        }
    }

}

