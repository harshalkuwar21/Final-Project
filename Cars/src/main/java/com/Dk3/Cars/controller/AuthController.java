package com.Dk3.Cars.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private static final String SESSION_ROLE = "USER_ROLE";

    private String redirectByRole(String role) {
        if ("ROLE_ADMIN".equals(role)) {
            return "redirect:/dashboard";
        }
        if ("ROLE_USER".equals(role)) {
            return "redirect:/user-dashboard";
        }
        return "redirect:/staff-dashboard";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping({"/login", "/"})
    public String loginPage(HttpSession session) {
        String role = (String) session.getAttribute(SESSION_ROLE);
        if (role != null) {
            return redirectByRole(role);
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboardpage(HttpSession session) {
        String role = (String) session.getAttribute(SESSION_ROLE);
        if (role == null) {
            return "redirect:/login";
        }
        if (!"ROLE_ADMIN".equals(role)) {
            return redirectByRole(role);
        }
        return "dashboard";
    }

    @GetMapping("/staff-dashboard")
    public String staffDashboardPage(HttpSession session) {
        String role = (String) session.getAttribute(SESSION_ROLE);
        if (role == null) {
            return "redirect:/login";
        }
        if ("ROLE_ADMIN".equals(role) || "ROLE_USER".equals(role)) {
            return redirectByRole(role);
        }
        return "staff-dashboard";
    }

    @GetMapping("/user-dashboard")
    public String userDashboardPage(HttpSession session) {
        String role = (String) session.getAttribute(SESSION_ROLE);
        if (role == null) {
            return "redirect:/login";
        }
        if (!"ROLE_USER".equals(role)) {
            return redirectByRole(role);
        }
        return "user-dashboard";
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
    public String carsPage(@RequestParam(required = false) Long showroomId,
                           @RequestParam(required = false) String name,
                           org.springframework.ui.Model model) {
        if (showroomId == null) {
            return "redirect:/cars/all";
        }
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
