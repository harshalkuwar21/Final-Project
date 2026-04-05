package com.Dk3.Cars.service;

import com.Dk3.Cars.dto.UserDto;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User register(UserDto userDto) {
        String email = userDto.getEmail() == null ? "" : userDto.getEmail().trim();
        String contact = normalizeDigits(userDto.getContact(), 10);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }
        if (!contact.matches("\\d{10}")) {
            throw new RuntimeException("Mobile number must be exactly 10 digits!");
        }

        User user = new User();
        user.setFirst(userDto.getFirst());
        user.setLast(userDto.getLast());
        user.setEmail(email);
        user.setContact(contact);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Set default role for newly registered users
        user.setRole("ROLE_USER");

        // 🔴 IMPORTANT FOR EMAIL VERIFICATION
        user.setEnabled(false);

        return userRepository.save(user);
    }

    private String normalizeDigits(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String digits = value.replaceAll("\\D", "");
        return digits.length() > maxLength ? digits.substring(0, maxLength) : digits;
    }
}
