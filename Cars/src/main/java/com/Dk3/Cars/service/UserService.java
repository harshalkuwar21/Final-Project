package com.Dk3.Cars.service;

import com.Dk3.Cars.dto.UserDto;
import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.mapper.UserConversion;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserConversion userConversion;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserDto register(UserDto userDto) {

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered!");
        }

        User user = new User();
        user.setFirst(userDto.getFirst());
        user.setLast(userDto.getLast());
        user.setEmail(userDto.getEmail());
        user.setContact(userDto.getContact());
        user.setPassword(encoder.encode(userDto.getPassword()));

        User saveduser = userRepository.save(user);
        
        return userConversion.toDto(saveduser);
    }

//    public User login(String email, String rawPassword) {
//        Optional<User> user = userRepository.findByEmail(email);
//
//        if (user == null) return null;
//
//        if (encoder.matches(rawPassword, user.getPassword())) {
//            return user;
//        }
//
//        return null;
//    }
}

