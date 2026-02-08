package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
public class TestDrive {
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

    private String status; // Scheduled / Completed / Cancelled
    private LocalDateTime scheduledDateTime;
    private LocalDateTime completedDateTime;

    @Column(length = 1000)
    private String feedback;

    private boolean convertedToSale = false;
}