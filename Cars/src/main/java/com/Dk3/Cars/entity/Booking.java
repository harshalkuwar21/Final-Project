package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "sales_executive_id")
    private User salesExecutive;

    private String status; // Pending / Confirmed / Cancelled
    private double bookingAmount;
    private String paymentMode;
    private LocalDate expectedDeliveryDate;
    private LocalDate bookingDate;

    @PrePersist
    private void prePersist() {
        if (bookingDate == null) {
            bookingDate = LocalDate.now();
        }
    }
}