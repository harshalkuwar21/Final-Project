package com.Dk3.Cars.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardpage() {
        return "dashboard";
    }

    @GetMapping("/showrooms")
    public String inventoryPage() {
        return "showrooms";
    }

    @GetMapping("/showrooms/add")
    public String addShowroomPage() {
        return "showroom-add";
    }

    @GetMapping("/cars")
    public String carsPage(@RequestParam Long showroomId, @RequestParam(required = false) String name, org.springframework.ui.Model model) {
        model.addAttribute("showroomId", showroomId);
        model.addAttribute("showroomName", name);
        return "cars";
    }

    @GetMapping("/staff")
    public String staffpage() {
        return "staff";
    }

    @GetMapping("/sales")
    public String salesPage() {
        return "sales";
    }

    @GetMapping("/reports")
    public String reportsPage() {
        return "reports";
    }

    @GetMapping("/settings")
    public String settingsPage() {
        return "settings";
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }
}
