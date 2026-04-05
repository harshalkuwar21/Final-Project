package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.repository.ShowroomRepository;
import com.Dk3.Cars.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
        return "car-edit";
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
