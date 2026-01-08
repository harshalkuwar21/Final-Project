package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String buyerName;
    private LocalDate soldDate;
    private String status;

    @ManyToOne
    private Car car;
}
