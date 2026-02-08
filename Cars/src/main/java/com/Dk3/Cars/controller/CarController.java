package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.service.CarService;
import com.Dk3.Cars.repository.ShowroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/cars")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private ShowroomRepository showroomRepository;

    @GetMapping("/all")
    public String listCars(Model model) {
        List<Car> cars = carService.getAllCars();
        model.addAttribute("cars", cars);
        return "cars";
    }

    @GetMapping("/add")
    public String showAddCarForm(Model model) {
        model.addAttribute("showrooms", showroomRepository.findAll());
        return "car-add";
    }

    @GetMapping("/showroom/{id}")
    public String showroomCars(@PathVariable Long id, Model model) {
        List<Car> cars = carService.getCarRepository().findByShowroomIdOrderByIdDesc(id);
        model.addAttribute("cars", cars);
        model.addAttribute("showroomId", id);
        return "showroom-cars";
    }

    @GetMapping("/edit/{id}")
    public String showEditCarForm(@PathVariable Long id, Model model) {
        Car car = carService.getCarById(id).orElse(null);
        if (car == null) {
            return "redirect:/cars";
        }
        model.addAttribute("car", car);
        return "car-edit";
    }



    @GetMapping("/delete/{id}")
    public String deleteCar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        carService.deleteCar(id);
        redirectAttributes.addFlashAttribute("success", "Car deleted successfully!");
        return "redirect:/cars";
    }

    @GetMapping("/search")
    public String searchCars(@RequestParam String keyword, Model model) {
        List<Car> cars = carService.searchCars(keyword);
        model.addAttribute("cars", cars);
        model.addAttribute("searchKeyword", keyword);
        return "cars";
    }
}