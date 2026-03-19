package com.Dk3.Cars.restcontroller;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomersRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    

    @GetMapping
    public List<User> list() {
        return userRepository.findByRole("ROLE_USER");
    }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        String email = body.get("email") == null ? null : String.valueOf(body.get("email")).trim();
        String contact = normalizeContact(body.get("contact"));
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            resp.put("ok", false);
            resp.put("message", "Email already exists");
            return ResponseEntity.badRequest().body(resp);
        }
        if (contact == null || !contact.matches("\\d{10}")) {
            resp.put("ok", false);
            resp.put("message", "Mobile number must be exactly 10 digits");
            return ResponseEntity.badRequest().body(resp);
        }
        User u = new User();
        u.setFirst(body.get("first") == null ? null : String.valueOf(body.get("first")).trim());
        u.setLast(body.get("last") == null ? null : String.valueOf(body.get("last")).trim());
        u.setEmail(email);
        u.setContact(contact);
        u.setRole("ROLE_USER");
        u.setEnabled(true);
        u.setActive(true);
        String password = body.get("password") == null ? null : String.valueOf(body.get("password"));
        if (password == null || password.isBlank()) {
            u.setPassword(passwordEncoder.encode("changeme123"));
        } else {
            u.setPassword(passwordEncoder.encode(password));
        }
        User saved = userRepository.save(u);
        resp.put("ok", true);
        resp.put("user", saved);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Map<String, Object> resp = new HashMap<>();
        return userRepository.findById(id).map(existing -> {
            String email = body.get("email") == null ? null : String.valueOf(body.get("email")).trim();
            String contact = body.containsKey("contact") ? normalizeContact(body.get("contact")) : existing.getContact();
            if (email != null && !email.equals(existing.getEmail()) && userRepository.existsByEmail(email)) {
                resp.put("ok", false);
                resp.put("message", "Email already exists");
                return ResponseEntity.badRequest().body(resp);
            }
            if (contact == null || !contact.matches("\\d{10}")) {
                resp.put("ok", false);
                resp.put("message", "Mobile number must be exactly 10 digits");
                return ResponseEntity.badRequest().body(resp);
            }
            if (body.containsKey("first")) existing.setFirst(body.get("first") == null ? null : String.valueOf(body.get("first")).trim());
            if (body.containsKey("last")) existing.setLast(body.get("last") == null ? null : String.valueOf(body.get("last")).trim());
            if (body.containsKey("email")) existing.setEmail(email);
            if (body.containsKey("contact")) existing.setContact(contact);
            if (body.containsKey("password")) {
                String password = body.get("password") == null ? null : String.valueOf(body.get("password"));
                if (password != null && !password.isBlank()) {
                    existing.setPassword(passwordEncoder.encode(password));
                }
            }
            existing.setRole("ROLE_USER");
            if (body.containsKey("enabled")) {
                existing.setEnabled(Boolean.parseBoolean(String.valueOf(body.get("enabled"))));
            }
            if (body.containsKey("active")) {
                existing.setActive(Boolean.parseBoolean(String.valueOf(body.get("active"))));
            }
            User saved = userRepository.save(existing);
            resp.put("ok", true);
            resp.put("user", saved);
            return ResponseEntity.ok(resp);
        }).orElseGet(() -> {
            resp.put("ok", false);
            resp.put("message", "User not found");
            return ResponseEntity.status(404).body(resp);
        });
    }

    private String normalizeContact(Object value) {
        if (value == null) return null;
        return String.valueOf(value).replaceAll("\\D", "").trim();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }

    @PutMapping("/{id}/fraud-block")
    public ResponseEntity<?> fraudBlock(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        return userRepository.findById(id).map(existing -> {
            if (!"ROLE_USER".equals(existing.getRole())) {
                resp.put("ok", false);
                resp.put("message", "Only customer users can be blocked");
                return ResponseEntity.badRequest().body(resp);
            }

            existing.setEnabled(false);
            existing.setActive(false);
            User saved = userRepository.save(existing);
            resp.put("ok", true);
            resp.put("user", saved);
            return ResponseEntity.ok(resp);
        }).orElseGet(() -> {
            resp.put("ok", false);
            resp.put("message", "User not found");
            return ResponseEntity.status(404).body(resp);
        });
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        Map<String, Object> resp = new HashMap<>();
        return userRepository.findById(id).map(existing -> {
            if (!"ROLE_USER".equals(existing.getRole())) {
                resp.put("ok", false);
                resp.put("message", "Only customer users can be activated");
                return ResponseEntity.badRequest().body(resp);
            }

            existing.setEnabled(true);
            existing.setActive(true);
            User saved = userRepository.save(existing);
            resp.put("ok", true);
            resp.put("user", saved);
            return ResponseEntity.ok(resp);
        }).orElseGet(() -> {
            resp.put("ok", false);
            resp.put("message", "User not found");
            return ResponseEntity.status(404).body(resp);
        });
    }
}
