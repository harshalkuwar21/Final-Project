package com.Dk3.Cars.service;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    @Autowired
    private CarRepository carRepository;

    private final String UPLOAD_DIR = "uploads/cars/";

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Optional<Car> getCarById(Long id) {
        return carRepository.findById(id);
    }

    public Car saveCar(Car car) {
        return carRepository.save(car);
    }

    public void deleteCar(Long id) {
        carRepository.deleteById(id);
    }

    public List<Car> searchCars(String keyword) {
        return carRepository.searchCars(keyword);
    }

    public List<Car> getAvailableCars() {
        return carRepository.findAvailableCars();
    }

    public List<Car> getLowStockCars() {
        return carRepository.findLowStockCars();
    }

    public List<Object[]> getLowStockModels() {
        return carRepository.findLowStockModels();
    }

    public long getTotalCarsInStock() {
        return carRepository.countAvailableCars();
    }


    public void markCarAsSold(Long carId) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isPresent()) {
            Car car = carOpt.get();
            car.setStatus("Sold");
            car.setSold(true);
            carRepository.save(car);
        }
    }

    public CarRepository getCarRepository() {
        return carRepository;
    }

}