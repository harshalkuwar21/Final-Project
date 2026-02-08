package com.Dk3.Cars.controller;

import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/list")
    public String listStaff(Model model) {
        List<User> staff = userRepository.findByRoleNot("ROLE_USER");
        model.addAttribute("staff", staff);
        return "staff";
    }

    @GetMapping("/add")
    public String showAddStaffForm(Model model) {
        model.addAttribute("user", new User());
        return "staff-add";
    }

    @PostMapping("/add")
    public String addStaff(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true); // Staff accounts are enabled by default

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Staff member added successfully!");
        return "redirect:/staff";
    }

    @GetMapping("/edit/{id}")
    public String showEditStaffForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null || "ROLE_USER".equals(user.getRole())) {
            return "redirect:/staff";
        }
        model.addAttribute("user", user);
        return "staff-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateStaff(@PathVariable Long id, @ModelAttribute User user,
                             @RequestParam(required = false) String newPassword,
                             RedirectAttributes redirectAttributes) {
        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            return "redirect:/staff";
        }

        // Update fields
        existingUser.setFirst(user.getFirst());
        existingUser.setLast(user.getLast());
        existingUser.setEmail(user.getEmail());
        existingUser.setContact(user.getContact());
        existingUser.setRole(user.getRole());
        existingUser.setSalary(user.getSalary());
        existingUser.setSalesTarget(user.getSalesTarget());
        existingUser.setDepartment(user.getDepartment());
        existingUser.setActive(user.isActive());

        // Update password if provided
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(newPassword));
        }

        userRepository.save(existingUser);
        redirectAttributes.addFlashAttribute("success", "Staff member updated successfully!");
        return "redirect:/staff";
    }

    @GetMapping("/delete/{id}")
    public String deleteStaff(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && !"ROLE_USER".equals(user.getRole())) {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Staff member deleted successfully!");
        }
        return "redirect:/staff";
    }

    @GetMapping("/sales-executives")
    public String listSalesExecutives(Model model) {
        List<User> salesExecutives = userRepository.findByRole("ROLE_SALES_EXECUTIVE");
        model.addAttribute("staff", salesExecutives);
        model.addAttribute("roleFilter", "Sales Executives");
        return "staff";
    }

    @GetMapping("/managers")
    public String listManagers(Model model) {
        List<User> managers = userRepository.findByRole("ROLE_MANAGER");
        model.addAttribute("staff", managers);
        model.addAttribute("roleFilter", "Managers");
        return "staff";
    }

    @GetMapping("/active")
    public String listActiveStaff(Model model) {
        List<User> activeStaff = userRepository.findByActive(true);
        model.addAttribute("staff", activeStaff);
        model.addAttribute("statusFilter", "Active");
        return "staff";
    }
}