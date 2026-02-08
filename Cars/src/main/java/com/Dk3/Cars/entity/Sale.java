package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
public class Sale {

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
    private String buyerName; // Individual / Corporate
    private LocalDate soldDate;
    private double sellingPrice;
    private double discount = 0;
    private double gstAmount = 0;
    private double totalAmount;

    private String paymentMode; // Cash / UPI / Card / Bank Transfer
    private String status = "Paid"; // Paid / Pending / Refunded

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Payment> payments;

    @PrePersist
    private void prePersist() {
        if (soldDate == null) {
            soldDate = LocalDate.now();
        }
        // Calculate total amount
        totalAmount = sellingPrice - discount + gstAmount;
    }
}
