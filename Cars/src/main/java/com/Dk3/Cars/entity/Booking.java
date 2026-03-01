package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String workflowStatus; // Pending / Payment Verified / Approved / Car Allocated / Ready for Delivery / Delivered / Rejected
    private double bookingAmount;
    private String paymentMode;
    private String transactionId;
    private String paymentScreenshotUrl;
    private LocalDate expectedDeliveryDate;
    private String deliveryTimeSlot;
    private String deliveryType; // Showroom Pickup / Home Delivery
    private String rejectionReason;
    private LocalDate bookingDate;
    private LocalDateTime statusUpdatedAt;

    // Step 1 personal details
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String aadhaarNumber;
    private String panNumber;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String aadhaarPhotoUrl;
    private String panPhotoUrl;
    private String signaturePhotoUrl;
    private String passportPhotoUrl;

    // Generated documents after approval
    private String bookingReceiptUrl;
    private String proformaInvoiceUrl;
    private String allotmentLetterUrl;
    private String deliveryConfirmationLetterUrl;

    @PrePersist
    private void prePersist() {
        if (bookingDate == null) {
            bookingDate = LocalDate.now();
        }
        if (statusUpdatedAt == null) {
            statusUpdatedAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "Pending";
        }
        if (workflowStatus == null || workflowStatus.isBlank()) {
            workflowStatus = status;
        }
    }
}
