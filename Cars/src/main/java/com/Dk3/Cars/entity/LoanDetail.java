package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loan_details")
public class LoanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    private Boolean loanRequired;
    private String bankName;
    private Double salary;
    private String employmentType;
    private Double monthlyIncome;
    private String panNumber;
    private String aadhaarNumber;
    private Double interestRate;
    private Integer tenureMonths;
    private Double carPrice;
    private Double downPaymentAmount;
    private Double loanAmount;
    private Double emiAmount;
    private String status; // Pending / Approved / Rejected
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

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
