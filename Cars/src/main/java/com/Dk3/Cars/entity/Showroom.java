package com.Dk3.Cars.entity;



import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Showroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;
    private String address;
    private String contactNumber;
    private String mapUrl;
    private String workingHours;
    private String managerName;
    private String imageUrl;   // example: /images/showrooms/nashik.jpg

    // getters & setters
}
