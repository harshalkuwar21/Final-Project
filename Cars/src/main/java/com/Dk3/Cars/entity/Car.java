package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;
    private String brand;
    private double price;
    private boolean sold;
    @ManyToOne
    @JoinColumn(name = "showroom_id")
    private Showroom showroom;

}
