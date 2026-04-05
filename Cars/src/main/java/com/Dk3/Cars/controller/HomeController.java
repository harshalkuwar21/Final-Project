package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.HomeReview;
import com.Dk3.Cars.entity.Showroom;
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.HomeReviewRepository;
import com.Dk3.Cars.repository.SaleRepository;
import com.Dk3.Cars.repository.ShowroomRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private static final DateTimeFormatter REVIEW_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final CarRepository carRepository;
    private final ShowroomRepository showroomRepository;
    private final SaleRepository saleRepository;
    private final HomeReviewRepository homeReviewRepository;

    public HomeController(CarRepository carRepository,
                          ShowroomRepository showroomRepository,
                          SaleRepository saleRepository,
                          HomeReviewRepository homeReviewRepository) {
        this.carRepository = carRepository;
        this.showroomRepository = showroomRepository;
        this.saleRepository = saleRepository;
        this.homeReviewRepository = homeReviewRepository;
    }

    @GetMapping({"/", "/home"})
    public String homePage(Model model, HttpSession session) {
        List<Car> availableCars = carRepository.findAvailableCars();
        Map<Long, Long> showroomCarCounts = showroomRepository.getAvailableCarCountByShowroom()
                .stream()
                .filter(row -> row.length >= 2 && row[0] instanceof Number && row[1] instanceof Number)
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue(),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        List<String> brands = availableCars.stream()
                .map(Car::getBrand)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        List<Map<String, Object>> featuredCars = availableCars.stream()
                .sorted(Comparator
                        .comparing(this::carReviewSortValue, Comparator.reverseOrder())
                        .thenComparing(Car::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(car -> toCarCard(car, session))
                .toList();

        List<Map<String, Object>> showroomCards = showroomRepository.findAll().stream()
                .sorted(Comparator.comparing(showroom -> safe(showroom.getName()).toLowerCase(Locale.ROOT)))
                .limit(5)
                .map(showroom -> toShowroomCard(showroom, showroomCarCounts.getOrDefault(showroom.getId(), 0L), session))
                .toList();

        List<Map<String, Object>> brandCards = brands.stream()
                .limit(12)
                .map(this::toBrandCard)
                .toList();

        List<Map<String, Object>> reviewCards = homeReviewRepository.findTop6ByApprovedTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::toReviewCard)
                .toList();

        model.addAttribute("featuredCars", featuredCars);
        model.addAttribute("showrooms", showroomCards);
        model.addAttribute("brands", brandCards);
        model.addAttribute("reviews", reviewCards);
        model.addAttribute("carsAvailableCount", availableCars.size());
        model.addAttribute("brandsAvailableCount", brands.size());
        model.addAttribute("showroomsLiveCount", showroomCards.size());
        model.addAttribute("reviewCount", homeReviewRepository.countByApprovedTrue());
        model.addAttribute("deliveredCount", saleRepository.count());
        model.addAttribute("averageRating", formatAverageRating(homeReviewRepository.findAverageApprovedRating()));
        model.addAttribute("primaryActionUrl", primaryActionUrl(session));
        model.addAttribute("primaryActionLabel", primaryActionLabel(session));
        model.addAttribute("secondaryActionUrl", secondaryActionUrl(session));
        model.addAttribute("secondaryActionLabel", secondaryActionLabel(session));
        model.addAttribute("dashboardUrl", dashboardUrl(session));
        model.addAttribute("dashboardLabel", hasRole(session) ? "Dashboard" : "Login");
        model.addAttribute("isUserSession", "ROLE_USER".equals(session.getAttribute("USER_ROLE")));
        model.addAttribute("isLoggedIn", hasRole(session));
        return "Home";
    }

    private Map<String, Object> toBrandCard(String brand) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("name", brand);
        card.put("initial", brand.substring(0, 1).toUpperCase(Locale.ROOT));
        card.put("logoUrl", resolveBrandLogo(brand));
        return card;
    }

    private Map<String, Object> toCarCard(Car car, HttpSession session) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("id", car.getId());
        card.put("title", ((safe(car.getBrand()) + " " + safe(car.getModel())).trim()));
        card.put("price", formatCarPrice(car.getPrice()));
        card.put("fuelType", safeOrFallback(car.getFuelType(), "Premium Fuel Mix"));
        card.put("transmission", safeOrFallback(car.getTransmission(), "Automatic / Manual"));
        card.put("mileage", safeOrFallback(car.getMileage(), "Verified mileage"));
        card.put("showroomName", car.getShowroom() != null ? safeOrFallback(car.getShowroom().getName(), "DK3 Showroom") : "DK3 Showroom");
        card.put("imageUrl", resolveCarImage(car));
        card.put("rating", formatAverageRating(car.getReviewScore()));
        card.put("actionUrl", userCarActionUrl(session, car.getId()));
        card.put("actionLabel", userCarActionLabel(session));
        return card;
    }

    private Map<String, Object> toShowroomCard(Showroom showroom, long availableCarsCount, HttpSession session) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("id", showroom.getId());
        card.put("name", safeOrFallback(showroom.getName(), "DK3 Showroom"));
        card.put("city", safeOrFallback(showroom.getCity(), "India"));
        card.put("address", safeOrFallback(showroom.getAddress(), "Address will be updated soon"));
        card.put("contactNumber", safeOrFallback(showroom.getContactNumber(), "Contact at showroom"));
        card.put("type", safeOrFallback(showroom.getType(), "Premium"));
        card.put("workingHours", safeOrFallback(showroom.getWorkingHours(), "09:00 AM - 08:00 PM"));
        card.put("availableCarsCount", availableCarsCount);
        card.put("imageUrl", safeOrFallback(showroom.getImageUrl(), "/images/background.jpg"));
        card.put("actionUrl", showroomActionUrl(session));
        return card;
    }

    private Map<String, Object> toReviewCard(HomeReview review) {
        Map<String, Object> card = new LinkedHashMap<>();
        card.put("reviewerName", safeOrFallback(review.getReviewerName(), "DK3 Customer"));
        card.put("city", safeOrFallback(review.getCity(), "India"));
        card.put("showroomVisited", safeOrFallback(review.getShowroomVisited(), "DK3 Showroom"));
        card.put("title", safeOrFallback(review.getTitle(), "Customer Experience"));
        card.put("message", safeOrFallback(review.getMessage(), "Great experience with DK3 Cars."));
        card.put("rating", review.getRating() == null ? 5 : review.getRating());
        card.put("stars", buildStars(review.getRating() == null ? 5 : review.getRating()));
        card.put("createdAt", review.getCreatedAt() == null ? "" : REVIEW_DATE_FORMAT.format(review.getCreatedAt()));
        return card;
    }

    private String buildStars(int rating) {
        int safeRating = Math.max(1, Math.min(5, rating));
        return "\u2605".repeat(safeRating) + "\u2606".repeat(5 - safeRating);
    }

    private String resolveBrandLogo(String brand) {
        if (brand == null || brand.isBlank()) {
            return "https://via.placeholder.com/80x80.png?text=DK3";
        }
        String normalized = brand.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "toyota" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/17/brands/logos/toyota.jpg";
            case "tata" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/16/brands/logos/tata.jpg";
            case "mercedes", "mercedes-benz" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/11/brands/logos/mercedes-benz.jpg";
            case "audi" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/18/brands/logos/audi.jpg";
            case "bmw" -> "https://upload.wikimedia.org/wikipedia/commons/4/44/BMW.svg";
            case "maruti suzuki", "maruti" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/10/brands/logos/maruti-suzuki1647009823420.jpg";
            case "mahindra" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/9/brands/logos/mahindra.jpg";
            case "kia" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/70/brands/logos/kia.jpg";
            case "skoda" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/15/brands/logos/skoda1681982956420.jpg";
            case "land rover" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/23/brands/logos/land-rover1647236056893.jpg";
            case "hyundai" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/8/brands/logos/hyundai.jpg";
            case "honda" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/7/brands/logos/honda.jpg";
            case "volkswagen" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/20/brands/logos/volkswagen.jpg";
            case "jeep" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/43/brands/logos/jeep.jpg";
            case "mg" -> "https://imgd.aeplcdn.com/0X0/n/cw/ec/72/brands/logos/mg.jpg";
            default -> "https://via.placeholder.com/80x80.png?text=" + brand.substring(0, 1).toUpperCase(Locale.ROOT);
        };
    }

    private String resolveCarImage(Car car) {
        if (car.getImageUrls() != null && !car.getImageUrls().isEmpty() && car.getImageUrls().get(0) != null
                && !car.getImageUrls().get(0).isBlank()) {
            return car.getImageUrls().get(0);
        }
        return "/images/car.jpg";
    }

    private Double carReviewSortValue(Car car) {
        return car.getReviewScore() == null ? 0D : car.getReviewScore();
    }

    private String formatCarPrice(double value) {
        if (value <= 0) {
            return "Price on request";
        }
        if (value >= 10000000) {
            return String.format("Rs %.2f Crore", value / 10000000D);
        }
        return String.format("Rs %.2f Lakh", value / 100000D);
    }

    private String formatAverageRating(Double value) {
        if (value == null || value <= 0) {
            return "New";
        }
        return String.format("%.1f/5", value);
    }

    private String userCarActionUrl(HttpSession session, Long carId) {
        if ("ROLE_USER".equals(session.getAttribute("USER_ROLE"))) {
            return "/user-panel/car-details?carId=" + carId;
        }
        return primaryActionUrl(session);
    }

    private String userCarActionLabel(HttpSession session) {
        if ("ROLE_USER".equals(session.getAttribute("USER_ROLE"))) {
            return "View Details";
        }
        return primaryActionLabel(session);
    }

    private String showroomActionUrl(HttpSession session) {
        if ("ROLE_USER".equals(session.getAttribute("USER_ROLE"))) {
            return "/user-panel/showrooms";
        }
        return primaryActionUrl(session);
    }

    private String primaryActionUrl(HttpSession session) {
        Object role = session.getAttribute("USER_ROLE");
        if ("ROLE_USER".equals(role)) {
            return "/user-panel/cars";
        }
        if (role != null) {
            return dashboardUrl(session);
        }
        return "/login";
    }

    private String primaryActionLabel(HttpSession session) {
        Object role = session.getAttribute("USER_ROLE");
        if ("ROLE_USER".equals(role)) {
            return "Explore Cars";
        }
        if (role != null) {
            return "Open Dashboard";
        }
        return "Login to Explore";
    }

    private String secondaryActionUrl(HttpSession session) {
        return hasRole(session) ? "/logout" : "/register";
    }

    private String secondaryActionLabel(HttpSession session) {
        return hasRole(session) ? "Logout" : "Register";
    }

    private String dashboardUrl(HttpSession session) {
        Object role = session.getAttribute("USER_ROLE");
        if ("ROLE_ADMIN".equals(role)) {
            return "/dashboard";
        }
        if ("ROLE_USER".equals(role)) {
            return "/user-dashboard";
        }
        if (role != null) {
            return "/staff-dashboard";
        }
        return "/login";
    }

    private boolean hasRole(HttpSession session) {
        return session.getAttribute("USER_ROLE") != null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
