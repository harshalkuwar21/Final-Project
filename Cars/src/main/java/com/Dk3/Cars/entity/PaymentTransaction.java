package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String paymentType; // Booking / Down Payment / Final / EMI
    private Double amount;
    private String paymentMethod; // UPI / Card / Net Banking / RTGS / NEFT / Bank Transfer
    private String paymentGateway; // Razorpay / Paytm / Stripe / Offline
    private String transactionId;
    private String referenceNumber;
    private String receiptUrl;
    private String status; // Completed / Pending / Failed / Verified
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "Pending";
        }
    }
}
