package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    private Long userid;

    private String first;
    private String last;
    private String email;
    private String contact;
    @JsonIgnore
    private String password;
    private boolean enabled = false;
    private String role = "ROLE_USER"; // ROLE_ADMIN, ROLE_SALES_EXECUTIVE, ROLE_MANAGER, ROLE_ACCOUNTANT

    // Additional staff fields
    private String employeeId;
    private LocalDate joinDate;
    private double salary;
    private double salesTarget;
    private String department;
    private boolean active = true;
    private Long showroomId;

    @PrePersist
    private void prePersist() {
        if (joinDate == null) {
            joinDate = LocalDate.now();
        }
    }
} 
