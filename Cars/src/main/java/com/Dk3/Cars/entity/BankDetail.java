package com.Dk3.Cars.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bank_details")
public class BankDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;
    private String accountHolderName;
    private String accountNumberMasked;
    private String ifscCode;
    private String branchName;
    private boolean active = true;
}
