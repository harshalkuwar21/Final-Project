package com.Dk3.Cars.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long userid;
    private String first;
    private String last;
    private String email;
    private String contact;
    private String password;
    private String role;
} 
