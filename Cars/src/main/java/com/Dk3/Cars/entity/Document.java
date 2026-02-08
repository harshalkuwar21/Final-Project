package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String documentType; // RC / Insurance / Invoice / Form20 / Form21 / CustomerID
    private String fileName;
    private String filePath;
    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private LocalDate expiryDate; // For insurance, warranty
    private LocalDate uploadDate;

    @PrePersist
    private void prePersist() {
        if (uploadDate == null) {
            uploadDate = LocalDate.now();
        }
    }
}