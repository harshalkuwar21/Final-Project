package com.Dk3.Cars.restcontroller;

import com.Dk3.Cars.dto.UserDto;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.UserRepository;
import com.Dk3.Cars.service.EmailService;
import com.Dk3.Cars.service.UserService;
import com.Dk3.Cars.service.VerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserRestcontroller {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private VerificationTokenService tokenService;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Register user (saved as disabled)
            User user = userService.register(userDto);

            // Generate verification token
            String token = tokenService.createToken(user);

            // Send email
            emailService.sendVerificationEmail(user.getEmail(), "https://qp2d1349-9094.inc1.devtunnels.ms/verify?token=" + token);

            response.put("success", true);
            response.put("message", "Registration successful. Please verify your email.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> request,
            HttpSession session) {

        Map<String, String> response = new HashMap<>();

        String email = request.get("email") == null ? "" : request.get("email").trim();
        String password = request.get("password");

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Email or username not registered");
            return ResponseEntity.ok(response);
        }

        User user = optionalUser.get();

        // 🔴 EMAIL VERIFICATION CHECK
        if (!user.isEnabled()) {
            response.put("status", "error");
            response.put("message", "Please verify your email before login");
            return ResponseEntity.ok(response);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("status", "error");
            response.put("message", "Invalid password");
            return ResponseEntity.ok(response);
        }

        response.put("status", "success");
        response.put("message", "Login successful");
        response.put("role", user.getRole());

        String redirectUrl;
        if ("ROLE_ADMIN".equals(user.getRole())) {
            redirectUrl = "/dashboard";
        } else if ("ROLE_USER".equals(user.getRole())) {
            redirectUrl = "/user-dashboard";
        } else {
            redirectUrl = "/staff-dashboard";
        }

        session.setAttribute("USER_ID", user.getUserid());
        session.setAttribute("USER_EMAIL", user.getEmail());
        session.setAttribute("USER_ROLE", user.getRole());

        response.put("redirectUrl", redirectUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(java.util.Map.of(
                "status", "success",
                "message", "Logged out successfully"
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Not logged in"));
        }
        Long userId = Long.valueOf(String.valueOf(userIdObj));
        User u = userRepository.findById(userId).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(java.util.Map.of("ok", false, "error", "no user"));
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", u.getUserid());
        m.put("first", u.getFirst());
        m.put("last", u.getLast());
        m.put("email", u.getEmail());
        m.put("contact", u.getContact());
        m.put("profilePhotoUrl", u.getProfilePhotoUrl());
        m.put("role", u.getRole());
        m.put("enabled", u.isEnabled());
        return ResponseEntity.ok(m);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody java.util.Map<String, String> body, HttpSession session) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Not logged in"));
        }
        Long id = Long.valueOf(String.valueOf(userIdObj));
        var opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            resp.put("ok", false); resp.put("error","not found"); return ResponseEntity.status(404).body(resp);
        }
        User u = opt.get();
        String first = body.getOrDefault("first", u.getFirst());
        String last = body.getOrDefault("last", u.getLast());
        String email = body.getOrDefault("email", u.getEmail()).trim();
        String contact = normalizeDigits(body.getOrDefault("contact", u.getContact()), 10);
        String password = body.get("password");

        if (!email.equals(u.getEmail()) && userRepository.existsByEmail(email)) {
            resp.put("ok", false); resp.put("error","email exists"); return ResponseEntity.badRequest().body(resp);
        }
        if (!contact.isBlank() && !contact.matches("\\d{10}")) {
            resp.put("ok", false); resp.put("error","Mobile number must be exactly 10 digits"); return ResponseEntity.badRequest().body(resp);
        }

        u.setFirst(first); u.setLast(last); u.setEmail(email); u.setContact(contact);
        if (password != null && !password.isBlank()) {
            u.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(u);
        resp.put("ok", true);
        resp.put("user", java.util.Map.of(
                "id", u.getUserid(),
                "first", u.getFirst(),
                "last", u.getLast(),
                "email", u.getEmail(),
                "contact", u.getContact(),
                "profilePhotoUrl", u.getProfilePhotoUrl()
        ));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/profile/photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestPart("photo") MultipartFile photo, HttpSession session) {
        Object userIdObj = session.getAttribute("USER_ID");
        if (userIdObj == null) {
            return ResponseEntity.status(401).body(Map.of("ok", false, "error", "Not logged in"));
        }
        Long userId = Long.valueOf(String.valueOf(userIdObj));
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "error", "User not found"));
        }
        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "Photo is required"));
        }
        try {
            String url = saveFile(photo, "uploads/profile");
            user.setProfilePhotoUrl(url);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("ok", true, "profilePhotoUrl", url));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("ok", false, "error", "Unable to upload photo"));
        }
    }

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendForgotPasswordOtp(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        if (email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Email is required"));
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "message", "Email not found"));
        }
        String otp = String.valueOf((int) (100000 + Math.random() * 900000));
        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        emailService.sendOtpEmail(email, otp);
        return ResponseEntity.ok(Map.of("ok", true, "message", "OTP sent to your email"));
    }

    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<?> verifyOtpAndResetPassword(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "").trim();
        String otp = body.getOrDefault("otp", "").trim();
        String newPassword = body.getOrDefault("newPassword", "").trim();
        if (email.isBlank() || otp.isBlank() || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Email, OTP, and new password are required"));
        }
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "message", "Email not found"));
        }
        if (user.getResetOtp() == null || user.getResetOtpExpiry() == null || user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "OTP expired. Request new OTP"));
        }
        if (!otp.equals(user.getResetOtp())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", "Invalid OTP"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("ok", true, "message", "Password reset successful"));
    }

    @DeleteMapping("/forgot-password/clear-otp")
    public ResponseEntity<?> clearOtp(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("ok", false, "message", "Email not found"));
        }
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    private String saveFile(MultipartFile file, String folderPath) throws IOException {
        Path folder = Path.of(folderPath);
        Files.createDirectories(folder);
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot);
        String fileName = UUID.randomUUID() + ext;
        Path target = folder.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return "/" + folderPath.replace("\\", "/") + "/" + fileName;
    }

    private String normalizeDigits(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String digits = value.replaceAll("\\D", "");
        return digits.length() > maxLength ? digits.substring(0, maxLength) : digits;
    }
}
