package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.Showroom;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.repository.ShowroomRepository;
import com.Dk3.Cars.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/cars")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private ShowroomRepository showroomRepository;

    @Autowired
    private UserRepository userRepository;

    private boolean isStaffRole(String role) {
        return role != null && !"ROLE_ADMIN".equals(role) && !"ROLE_USER".equals(role);
    }

    private Optional<User> getSessionUser(HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) return Optional.empty();
        Long userId = Long.valueOf(String.valueOf(userIdObj));
        return userRepository.findById(userId);
    }

    private boolean matchesKeyword(Car car, String keyword) {
        String k = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);
        if (k.isBlank()) return true;
        return (car.getBrand() != null && car.getBrand().toLowerCase(Locale.ROOT).contains(k))
                || (car.getModel() != null && car.getModel().toLowerCase(Locale.ROOT).contains(k))
                || (car.getFuelType() != null && car.getFuelType().toLowerCase(Locale.ROOT).contains(k));
    }

    private boolean canAccessCar(HttpSession session, Car car) {
        String role = (String) session.getAttribute("USER_ROLE");
        if (!isStaffRole(role)) return true;
        Long staffShowroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
        Long carShowroomId = car != null && car.getShowroom() != null ? car.getShowroom().getId() : null;
        return staffShowroomId != null && staffShowroomId.equals(carShowroomId);
    }

    private void populateShowroomFormOptions(Model model, HttpSession session, Car car) {
        String role = (String) session.getAttribute("USER_ROLE");
        boolean staffUser = isStaffRole(role);
        model.addAttribute("isStaffUser", staffUser);

        if (staffUser) {
            Long showroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
            model.addAttribute("assignedShowroomId", showroomId);
            model.addAttribute("assignedShowroomName",
                    showroomId == null
                            ? "No showroom assigned"
                            : showroomRepository.findById(showroomId)
                                    .map(Showroom::getName)
                                    .orElse("Showroom #" + showroomId));
        } else {
            model.addAttribute("showrooms", showroomRepository.findAll());
            model.addAttribute("selectedShowroomId",
                    car != null && car.getShowroom() != null ? car.getShowroom().getId() : null);
        }
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

    private String getColorOptionColumn(String colorOptions, int index) {
        if (colorOptions == null || colorOptions.isBlank()) {
            return "";
        }
        List<String> values = new ArrayList<>();
        for (String entry : colorOptions.split("\\|")) {
            String cleaned = entry == null ? "" : entry.trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            String[] parts = cleaned.split("~", -1);
            values.add(index < parts.length ? parts[index].trim() : "");
        }
        return String.join("\n", values);
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

    private String getFaqColumn(String faqDetails, int index) {
        if (faqDetails == null || faqDetails.isBlank()) {
            return "";
        }
        List<String> values = new ArrayList<>();
        for (String entry : faqDetails.split("\\|")) {
            String cleaned = entry == null ? "" : entry.trim();
            if (cleaned.isEmpty()) {
                continue;
            }
            String[] parts = cleaned.split("~", -1);
            values.add(index < parts.length ? parts[index].trim() : "");
        }
        return String.join("\n", values);
    }

    @GetMapping("/all")
    public String listCars(Model model, HttpSession session) {
        String role = (String) session.getAttribute("USER_ROLE");
        List<Car> cars;

        if (isStaffRole(role)) {
            Optional<User> userOpt = getSessionUser(session);
            Long showroomId = userOpt.map(User::getShowroomId).orElse(null);
            if (showroomId == null) {
                cars = java.util.Collections.emptyList();
                model.addAttribute("error", "No showroom is assigned to this staff account.");
            } else {
                cars = carService.getCarRepository().findByShowroomIdOrderByIdDesc(showroomId);
                model.addAttribute("staffShowroomId", showroomId);
                model.addAttribute("staffShowroomName",
                        showroomRepository.findById(showroomId).map(s -> s.getName()).orElse("Showroom #" + showroomId));
            }
        } else {
            cars = carService.getAllCars();
        }

        model.addAttribute("cars", cars);
        return "cars";
    }

    @GetMapping("/add")
    public String showAddCarForm(Model model, HttpSession session) {
        String role = (String) session.getAttribute("USER_ROLE");
        boolean staffUser = isStaffRole(role);
        model.addAttribute("isStaffUser", staffUser);

        if (staffUser) {
            Optional<User> userOpt = getSessionUser(session);
            Long showroomId = userOpt.map(User::getShowroomId).orElse(null);
            if (showroomId == null) {
                model.addAttribute("showroomAllocationError", "No showroom allocated to your staff account.");
            } else {
                model.addAttribute("assignedShowroomId", showroomId);
                model.addAttribute("assignedShowroomName",
                        showroomRepository.findById(showroomId).map(s -> s.getName()).orElse("Showroom #" + showroomId));
            }
        } else {
            model.addAttribute("showrooms", showroomRepository.findAll());
        }

        return "car-add";
    }

    @GetMapping("/showroom/{id}")
    public String showroomCars(@PathVariable Long id, Model model, HttpSession session) {
        String role = (String) session.getAttribute("USER_ROLE");
        Long showroomId = id;

        if (isStaffRole(role)) {
            Optional<User> userOpt = getSessionUser(session);
            Long assignedShowroom = userOpt.map(User::getShowroomId).orElse(null);
            if (assignedShowroom == null) {
                model.addAttribute("cars", java.util.Collections.emptyList());
                model.addAttribute("showroomAllocationError", "No showroom allocated to your staff account.");
                model.addAttribute("showroomId", id);
                return "showroom-cars";
            }
            showroomId = assignedShowroom;
        }

        List<Car> cars = carService.getCarRepository().findByShowroomIdOrderByIdDesc(showroomId);
        model.addAttribute("cars", cars);
        model.addAttribute("showroomId", showroomId);
        return "showroom-cars";
    }

    @GetMapping("/edit/{id}")
    public String showEditCarForm(@PathVariable Long id, Model model, HttpSession session) {
        Car car = carService.getCarById(id).orElse(null);
        if (car == null || !canAccessCar(session, car)) {
            return "redirect:/cars/all";
        }
        model.addAttribute("car", car);
        model.addAttribute("existingImageUrlsText",
                car.getImageUrls() == null ? "" : String.join("\n", car.getImageUrls()));
        model.addAttribute("existingColorOptionsText",
                car.getColorOptions() == null ? "" : car.getColorOptions().replace("|", "\n"));
        model.addAttribute("existingColorOptionNamesText", getColorOptionColumn(car.getColorOptions(), 0));
        model.addAttribute("existingColorOptionCodesText", getColorOptionColumn(car.getColorOptions(), 1));
        model.addAttribute("existingColorOptionImagesText", getColorOptionColumn(car.getColorOptions(), 2));
        model.addAttribute("existingFaqQuestionsText", getFaqColumn(car.getFaqDetails(), 0));
        model.addAttribute("existingFaqAnswersText", getFaqColumn(car.getFaqDetails(), 1));
        populateShowroomFormOptions(model, session, car);
        return "car-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCar(@PathVariable Long id,
                            @RequestParam String brand,
                            @RequestParam String model,
                            @RequestParam(required = false) String variant,
                            @RequestParam(required = false) String fuelType,
                            @RequestParam(required = false) String transmission,
                            @RequestParam(required = false) String mileage,
                            @RequestParam(required = false) String color,
                            @RequestParam double price,
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
                            @RequestParam(required = false) String imageUrls,
                            @RequestParam(required = false) MultipartFile[] images,
                            RedirectAttributes redirectAttributes,
                            HttpSession session) {
        Car car = carService.getCarById(id).orElse(null);
        if (car == null || !canAccessCar(session, car)) {
            redirectAttributes.addFlashAttribute("error", "Access denied for this vehicle.");
            return "redirect:/cars/all";
        }

        Long effectiveShowroomId = showroom;
        String role = (String) session.getAttribute("USER_ROLE");
        if (isStaffRole(role)) {
            effectiveShowroomId = getSessionUser(session).map(User::getShowroomId).orElse(null);
        }

        if (effectiveShowroomId == null) {
            redirectAttributes.addFlashAttribute("error", "Showroom is required to update the vehicle.");
            return "redirect:/cars/edit/" + id;
        }

        Showroom selectedShowroom = showroomRepository.findById(effectiveShowroomId).orElse(null);
        if (selectedShowroom == null) {
            redirectAttributes.addFlashAttribute("error", "Selected showroom does not exist.");
            return "redirect:/cars/edit/" + id;
        }

        car.setBrand(brand);
        car.setModel(model);
        car.setVariant(variant);
        car.setFuelType(fuelType);
        car.setTransmission(transmission);
        car.setMileage(mileage);
        car.setColor(color);
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
        car.setStatus(status != null && !status.isBlank() ? status : "Available");
        car.setSold("Sold".equalsIgnoreCase(car.getStatus()));
        car.setStockQuantity(stockQuantity != null ? stockQuantity : 1);
        car.setShowroom(selectedShowroom);

        if (purchaseDate != null && !purchaseDate.isBlank()) {
            car.setPurchaseDate(LocalDate.parse(purchaseDate));
        } else {
            car.setPurchaseDate(null);
        }

        List<String> finalImageUrls = new ArrayList<>();
        if (imageUrls != null && !imageUrls.isBlank()) {
            for (String rawUrl : imageUrls.split("[\\r\\n,]+")) {
                String cleanedUrl = rawUrl == null ? "" : rawUrl.trim();
                if (!cleanedUrl.isEmpty()) {
                    finalImageUrls.add(cleanedUrl);
                }
            }
        }

        if (images != null && images.length > 0) {
            try {
                String uploadDir = "uploads/cars/";
                Files.createDirectories(Paths.get(uploadDir));
                for (MultipartFile image : images) {
                    if (image == null || image.isEmpty()) {
                        continue;
                    }
                    String original = image.getOriginalFilename();
                    String safeName = original == null ? "car-image" : original.replaceAll("\\s+", "_");
                    String fileName = UUID.randomUUID() + "_" + safeName;
                    Path target = Paths.get(uploadDir, fileName);
                    Files.write(target, image.getBytes());
                    finalImageUrls.add("/uploads/cars/" + fileName);
                }
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error", "Vehicle details updated, but image upload failed.");
                carService.saveCar(car);
                return "redirect:/cars/all";
            }
        }

        car.setImageUrls(finalImageUrls);

        carService.saveCar(car);
        redirectAttributes.addFlashAttribute("success", "Vehicle updated successfully!");
        return "redirect:/cars/all";
    }



    @GetMapping("/delete/{id}")
    public String deleteCar(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        Car car = carService.getCarById(id).orElse(null);
        if (car == null || !canAccessCar(session, car)) {
            redirectAttributes.addFlashAttribute("error", "Access denied for this vehicle.");
            return "redirect:/cars/all";
        }
        carService.deleteCar(id);
        redirectAttributes.addFlashAttribute("success", "Car deleted successfully!");
        return "redirect:/cars/all";
    }

    @GetMapping("/search")
    public String searchCars(@RequestParam String keyword, Model model, HttpSession session) {
        String role = (String) session.getAttribute("USER_ROLE");
        List<Car> cars;

        if (isStaffRole(role)) {
            Optional<User> userOpt = getSessionUser(session);
            Long showroomId = userOpt.map(User::getShowroomId).orElse(null);
            if (showroomId == null) {
                cars = java.util.Collections.emptyList();
                model.addAttribute("error", "No showroom is assigned to this staff account.");
            } else {
                cars = carService.getCarRepository().findByShowroomIdOrderByIdDesc(showroomId)
                        .stream()
                        .filter(c -> matchesKeyword(c, keyword))
                        .toList();
                model.addAttribute("staffShowroomId", showroomId);
            }
        } else {
            cars = carService.searchCars(keyword);
        }

        model.addAttribute("cars", cars);
        model.addAttribute("searchKeyword", keyword);
        return "cars";
    }
}
