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
    private String paymentGateway;
    private String paymentOutcome;
    private String paymentOption; // Full Payment / Loan Required
    private Double downPaymentAmount;
    private Boolean downPaymentVerified;
    private String downPaymentMethod;
    private String downPaymentReference;
    private String downPaymentReceiptUrl;
    private Double gstAmount;
    private Double rtoCharges;
    private Double roadTaxAmount;
    private Double insuranceAmount;
    private Double fastagCharges;
    private Double handlingCharges;
    private Double accessoriesAmount;
    private Double extendedWarrantyAmount;
    private Double tcsAmount;
    private Double totalAmount;
    private Double paidAmount;
    private Double remainingAmount;
    private String escrowStatus; // Secured / Released
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

    // Step 2: Digital pre-verification
    private String preVerificationStatus; // Pending / Pre-Verified / Rejected
    private Boolean customerNameMatched;
    private String preVerificationRemarks;
    private String preVerifiedBy;
    private LocalDateTime preVerifiedAt;

    // Step 4: Insurance generated
    private String insuranceCompanyName; // ICICI Lombard / HDFC ERGO
    private String insurancePolicyNumber;
    private String insuranceDocumentUrl;
    private LocalDateTime insuranceGeneratedAt;

    // Step 5: RTO applied and temporary registration
    private Boolean form20Submitted;
    private Boolean form21Submitted;
    private Boolean form22Submitted;
    private Boolean invoiceSubmittedToRto;
    private Boolean insuranceSubmittedToRto;
    private String rtoAuthority;
    private String rtoApplicationStatus; // Not Applied / Applied / TR Issued
    private String temporaryRegistrationNumber;
    private String temporaryRegistrationUrl;
    private LocalDateTime rtoAppliedAt;

    // Step 6: Delivery handover
    private Boolean originalDocumentsVerified;
    private Boolean physicalVerificationDone;
    private Boolean deliveryNoteSigned;
    private LocalDateTime deliveryCompletedAt;
    private String finalInvoiceUrl;
    private String registrationCertificateUrl;
    private String pucCertificateUrl;
    private String warrantyDocumentUrl;
    private String serviceBookUrl;
    private String deliveryNoteUrl;
    private String roadTaxReceiptUrl;
    private String financeSanctionLetterUrl;
    private String financeAgreementUrl;
    private String loanDocumentUrl;

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
        if (preVerificationStatus == null || preVerificationStatus.isBlank()) {
            preVerificationStatus = "Pending";
        }
        if (rtoApplicationStatus == null || rtoApplicationStatus.isBlank()) {
            rtoApplicationStatus = "Not Applied";
        }
        if (rtoAuthority == null || rtoAuthority.isBlank()) {
            rtoAuthority = "Ministry of Road Transport and Highways";
        }
    }
}
