package com.Dk3.Cars.config;

import com.Dk3.Cars.entity.Car;
import com.Dk3.Cars.entity.Showroom;
import com.Dk3.Cars.repository.CarRepository;
import com.Dk3.Cars.repository.ShowroomRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SampleCarDataInitializer implements ApplicationRunner {

    private final CarRepository carRepository;
    private final ShowroomRepository showroomRepository;

    public SampleCarDataInitializer(CarRepository carRepository, ShowroomRepository showroomRepository) {
        this.carRepository = carRepository;
        this.showroomRepository = showroomRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!carRepository.findByBrandAndModel("Kia", "Carens Clavis").isEmpty()) {
            return;
        }

        Showroom showroom = showroomRepository.findAll().stream().findFirst().orElseGet(() -> {
            Showroom s = new Showroom();
            s.setName("DK3 Cars Nashik");
            s.setCity("Nashik");
            s.setAddress("Mumbai Naka, Nashik");
            s.setContactNumber("+91-9876543210");
            s.setWorkingHours("10:00 AM - 8:00 PM");
            s.setManagerName("DK3 Manager");
            s.setImageUrl("/images/background.jpg");
            return showroomRepository.save(s);
        });

        Car car = new Car();
        car.setBrand("Kia");
        car.setModel("Carens Clavis");
        car.setVariant("HTE Petrol 1.5L Manual 7 STR");
        car.setFuelType("Petrol");
        car.setTransmission("Manual");
        car.setMileage("16 kmpl");
        car.setColor("Intense Red");
        car.setPrice(1311000);
        car.setStatus("Available");
        car.setEngineCc("1482 cc / 1493 cc / 1497 cc");
        car.setSafetyRating("4 Star");
        car.setSeatingCapacity("6 & 7 Seater");
        car.setFuelOptions("Petrol & Diesel");
        car.setTransmissionOptions("Manual, Clutchless Manual (IMT), Automatic");
        car.setMileageDetails("Petrol (Manual):16 kmpl|Petrol - Automatic (DCT):16.66 kmpl|Diesel (Manual):19.54 kmpl|Diesel - Automatic (TC):17.5 kmpl|Petrol - Clutchless Manual (IMT):15.95 kmpl");
        car.setVariantDetails("Carens Clavis HTE~Petrol 1.5L Manual 7 STR - 113 bhp - 144 Nm~1311000|Carens Clavis HTE (O)~Petrol 1.5L Turbo Manual 7 STR - 158 bhp - 253 Nm - 15.95 kmpl~1524000|Carens Clavis HTE (O) Diesel~Diesel 1.5L Turbo Manual 7 STR - 114 bhp - 250 Nm - 19.54 kmpl~1682000|Carens Clavis HTK Plus~Diesel 1.5L Turbo Manual 7 STR - 114 bhp - 250 Nm - 19.54 kmpl~1904000|Carens Clavis HTX~Petrol 1.5L Turbo Clutchless Manual (IMT) 7 STR - 158 bhp - 253 Nm - 15.95 kmpl~2117000|Carens Clavis HTX (O)~Petrol 1.5L Turbo Automatic (DCT) 7 STR - 158 bhp - 253 Nm - 16.66 kmpl~2277000");
        car.setColorOptions("Intense Red~#fc0505~https://imgd.aeplcdn.com/370x208/n/tpej3cb_1726565.jpg?q=80|Gravity Grey~#5c5c5c~https://imgd.aeplcdn.com/600x337/n/afn7ufb_1833767.jpg?q=80|Pewter Olive~#05471b~https://imgd.aeplcdn.com/600x337/n/gs57ufb_1833773.jpg?q=80|Imperial Blue~#202151~https://imgd.aeplcdn.com/600x337/n/p077ufb_1833765.jpg?q=80");
        car.setReviewScore(4.7);
        car.setReviewExterior(4.8);
        car.setReviewPerformance(4.6);
        car.setReviewValue(4.7);
        car.setReviewFuelEconomy(4.2);
        car.setReviewComfort(4.9);
        car.setFaqDetails("What is the on road price of Kia Carens Clavis base model?~The on road price starts around Rs. 13.11 Lakh including registration, insurance and charges.|What is the real world versus claimed mileage of Kia Carens Clavis?~Claimed mileage is up to 19.54 kmpl while real world mileage is typically lower by driving conditions.|How many airbags does Kia Carens Clavis get?~Top variants provide up to 6 airbags including front, side and curtain airbags.|What is the on road price of Kia Carens Clavis top model?~The top model on-road price goes up to around Rs. 24.52 Lakh depending on city taxes.");
        car.setVin("VIN-KIA-CARENS-CLAVIS-001");
        car.setEngineNo("ENG-KIA-CARENS-CLAVIS-001");
        car.setPurchaseDate(LocalDate.of(2026, 2, 1));
        car.setSupplierInfo("Kia India Pvt Ltd");
        car.setStockQuantity(5);
        car.setShowroom(showroom);
        car.setImageUrls(List.of(
                "https://imgd.aeplcdn.com/664x374/n/cw/ec/195199/carens-clavis-exterior-right-front-three-quarter-3.png?isig=0&q=80",
                "https://imgd.aeplcdn.com/664x374/n/cw/ec/195199/clavis-interior-dashboard-3.jpeg?isig=0&q=80",
                "https://imgd.aeplcdn.com/664x374/n/cw/ec/195199/clavis-interior-instrument-cluster.jpeg?isig=0&q=80",
                "https://imgd.aeplcdn.com/664x374/n/cw/ec/195199/clavis-interior-third-row-seats.jpeg?isig=0&q=80",
                "https://imgd.aeplcdn.com/664x374/n/cw/ec/195199/clavis-interior-dashboard-switches.jpeg?isig=0&q=80",
                "https://imgd.aeplcdn.com/664x374/n/cw/ec/195199/clavis-interior-360-degree-camera-control.jpeg?isig=0&q=80"
        ));

        carRepository.save(car);
    }
}

