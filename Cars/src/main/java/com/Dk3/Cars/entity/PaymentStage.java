package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_stages")
public class PaymentStage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String stageName; // Booking Paid / Down Payment Paid / Loan Approved / Final Amount Received / Delivery Ready
    private Integer stageOrderNo;
    private String stageStatus; // Completed / Pending / Not Required
    private String remarks;
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        updatedAt = LocalDateTime.now();
        if (stageStatus == null || stageStatus.isBlank()) {
            stageStatus = "Pending";
        }
    }
}
