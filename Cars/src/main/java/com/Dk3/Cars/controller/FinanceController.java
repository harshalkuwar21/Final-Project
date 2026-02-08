package com.Dk3.Cars.controller;

import com.Dk3.Cars.repository.PaymentRepository;
import com.Dk3.Cars.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FinanceController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/finance")
    public String financePage(Model model) {
        // Add summary statistics
        Double todayCash = paymentService.getTodayCashCollection();
        Double totalPending = paymentRepository.findByStatus("Pending").stream()
            .mapToDouble(p -> p.getAmount()).sum();
        long totalPayments = paymentRepository.count();
        
        model.addAttribute("todayCash", todayCash != null ? todayCash : 0);
        model.addAttribute("totalPending", totalPending);
        model.addAttribute("totalPayments", totalPayments);
        model.addAttribute("payments", paymentService.getAllPayments());
        
        return "finance";
    }
}