package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
public class ServiceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    private LocalDate serviceDate;
    private String serviceType; // Regular / Repair / Warranty
    private String description;
    private double cost;
    private String servicedBy; // Workshop name
    private LocalDate nextServiceDate;
    private LocalDate warrantyExpiryDate;

    @PrePersist
    private void prePersist() {
        if (serviceDate == null) {
            serviceDate = LocalDate.now();
        }
    }
}