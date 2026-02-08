package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private double amount;
    private String paymentMethod; // Cash / UPI / Card / Bank Transfer
    private LocalDate paymentDate;
    private String status; // Completed / Pending / Failed / Refunded
    private String transactionId;

    @PrePersist
    private void prePersist() {
        if (paymentDate == null) {
            paymentDate = LocalDate.now();
        }
    }
}