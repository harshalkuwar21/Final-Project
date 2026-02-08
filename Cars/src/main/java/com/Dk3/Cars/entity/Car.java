package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private String variant;
    private String fuelType; // Petrol/Diesel/EV/CNG
    private String transmission;
    private String color;
    private double price;
    private String status; // Available / Sold / Reserved / Under Maintenance

    private String vin; // VIN / Chassis No
    private String engineNo;
    private LocalDate purchaseDate;
    private String supplierInfo; // Supplier / Dealer Info

    @ElementCollection
    private List<String> imageUrls; // Multiple images

    private boolean sold = false;
    private int stockQuantity = 1; // For inventory control

    @ManyToOne
    @JoinColumn(name = "showroom_id")
    private Showroom showroom;

    @Transient
    private boolean available;

    @PostLoad
    private void postLoad() {
        // compute transient available from persisted sold value and status
        this.available = !this.sold && "Available".equals(this.status);
    }

    @PrePersist
    @PreUpdate
    private void prePersistOrUpdate() {
        // ensure sold column is consistent with available flag
        if ("Sold".equals(this.status)) {
            this.sold = true;
        }
    }
}
