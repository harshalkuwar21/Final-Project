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

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        User user = new User();
        user.setFirst(userDto.getFirst());
        user.setLast(userDto.getLast());
        user.setEmail(userDto.getEmail());
        user.setContact(userDto.getContact());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Set default role for newly registered users
        user.setRole("ROLE_USER");

        // 🔴 IMPORTANT FOR EMAIL VERIFICATION
        user.setEnabled(false);

        return userRepository.save(user);
    }
}
