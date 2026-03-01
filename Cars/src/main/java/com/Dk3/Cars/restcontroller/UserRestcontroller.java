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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
            emailService.sendVerificationEmail(user.getEmail(), "https://7mf91x75-9094.inc1.devtunnels.ms/verify?token=" + token);

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

        String email = request.get("email");
        String password = request.get("password");

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Email not registered");
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
    public ResponseEntity<?> getProfile() {
        // return the first admin/staff user, or fallback to any user
        java.util.List<User> staff = userRepository.findByRole("ROLE_STAFF");
        User u = null;
        if (!staff.isEmpty()) u = staff.get(0);
        else u = userRepository.findAll().stream().findFirst().orElse(null);
        if (u == null) return ResponseEntity.status(404).body(java.util.Map.of("ok", false, "error", "no user"));
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        m.put("id", u.getUserid());
        m.put("first", u.getFirst());
        m.put("last", u.getLast());
        m.put("email", u.getEmail());
        m.put("contact", u.getContact());
        m.put("role", u.getRole());
        m.put("enabled", u.isEnabled());
        return ResponseEntity.ok(m);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody java.util.Map<String, String> body) {
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        String idStr = body.get("id");
        if (idStr == null) {
            resp.put("ok", false);
            resp.put("error", "id required");
            return ResponseEntity.badRequest().body(resp);
        }
        Long id = Long.parseLong(idStr);
        var opt = userRepository.findById(id);
        if (opt.isEmpty()) {
            resp.put("ok", false); resp.put("error","not found"); return ResponseEntity.status(404).body(resp);
        }
        User u = opt.get();
        String first = body.getOrDefault("first", u.getFirst());
        String last = body.getOrDefault("last", u.getLast());
        String email = body.getOrDefault("email", u.getEmail());
        String contact = body.getOrDefault("contact", u.getContact());
        String password = body.get("password");

        if (!email.equals(u.getEmail()) && userRepository.existsByEmail(email)) {
            resp.put("ok", false); resp.put("error","email exists"); return ResponseEntity.badRequest().body(resp);
        }

        u.setFirst(first); u.setLast(last); u.setEmail(email); u.setContact(contact);
        if (password != null && !password.isBlank()) {
            u.setPassword(passwordEncoder.encode(password));
        }
        userRepository.save(u);
        resp.put("ok", true);
        resp.put("user", java.util.Map.of("id", u.getUserid(), "first", u.getFirst(), "last", u.getLast(), "email", u.getEmail(), "contact", u.getContact()));
        return ResponseEntity.ok(resp);
    }
}
