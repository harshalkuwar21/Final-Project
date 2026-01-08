package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.Showroom;
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.SaleRepository;
import com.Dk3.Cars.repository.ShowroomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.nio.file.*;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;




@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ShowroomRepository showroomRepository;

    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        Map<String, Object> map = new HashMap<>();

        long totalShowroom = showroomRepository.count();
        long soldCars = carRepository.countBySold(true);
        long availableCars = carRepository.countBySold(false);
        Double revenue = carRepository.totalRevenue();

        map.put("totalShowroom", totalShowroom);
        map.put("soldCars", soldCars);
        map.put("availableCars", availableCars);
        map.put("revenue", revenue == null ? 0 : revenue);

        return map;
    }


    // ================== SOLD CARS ==================
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

    // ================== TOP CARS ==================
    @GetMapping("/top-cars")
    public List<String> getTopCars() {

        return carRepository.findAll(PageRequest.of(0,4))
                .stream()
                .map(Car::getModel)
                .toList();
    }
    // ================= INVENTORY OVERVIEW =================
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


    // ================= TOP SELLING CARS =================
    // ================= TOP SELLING CARS =================
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
// ================= INVENTORY PAGE DATA =================


    // ================= SHOWROOM LIST =================
    @GetMapping("/showrooms")
    public List<Map<String, Object>> getShowrooms() {

        return showroomRepository.findAll()
                .stream()
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.getId());
                    map.put("name", s.getName());
                    map.put("city", s.getCity());
                    map.put("image", s.getImageUrl());
                    return map;
                })
                .toList();
    }


    // ================= CARS BY SHOWROOM =================
    @GetMapping("/showrooms/{showroomId}/cars")
    public List<Map<String, Object>> getCarsByShowroom(
            @PathVariable Long showroomId) {

        return carRepository.findByShowroomId(showroomId)
                .stream()
                .map(car -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("model", car.getModel());
                    map.put("brand", car.getBrand());
                    map.put("price", car.getPrice());
                    map.put("sold", car.isSold());
                    return map;
                })
                .toList();
    }
    // ================= ADD SHOWROOM =================
    @PostMapping("/showroom")
    public Showroom saveShowroom(
            @RequestParam String name,
            @RequestParam String city,
            @RequestParam MultipartFile image) throws Exception {

        String uploadDir = "uploads/";
        Files.createDirectories(Paths.get(uploadDir));

        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        Path path = Paths.get(uploadDir + fileName);
        Files.write(path, image.getBytes());

        Showroom showroom = new Showroom();
        showroom.setName(name);
        showroom.setCity(city);
        showroom.setImageUrl("/uploads/" + fileName);

        return showroomRepository.save(showroom);
    }

    // ================= GET SHOWROOMS =================

    // ================= GET CARS BY SHOWROOM =================
    @GetMapping("/showrooms/{id}/cars")
    public List<Map<String, Object>> getCars(@PathVariable Long id) {

        return carRepository.findByShowroomId(id)
                .stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("model", c.getModel());
                    map.put("brand", c.getBrand());
                    map.put("price", c.getPrice());
                    map.put("sold", c.isSold());
                    return map;
                })
                .toList();
    }

}
