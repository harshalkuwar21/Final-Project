package com.Dk3.Cars.controller;

import com.Dk3.Cars.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/customers")
    public String customersPage(Model model) {
        model.addAttribute("customers", customerRepository.findAll());
        return "customers";
    }
}
