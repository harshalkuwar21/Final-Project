package com.Dk3.Cars.config;

import com.Dk3.Cars.entity.User;
import com.Dk3.Cars.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminAccountInitializer {

    @Bean
    public ApplicationRunner adminBootstrapRunner(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            @Value("${app.admin.username}") String adminUsername,
            @Value("${app.admin.password}") String adminPassword,
            @Value("${app.admin.first-name:System}") String firstName,
            @Value("${app.admin.last-name:Admin}") String lastName) {

        return args -> {
            if (adminUsername == null || adminUsername.isBlank() || adminPassword == null || adminPassword.isBlank()) {
                return;
            }

            User adminUser = userRepository.findByEmail(adminUsername).orElseGet(User::new);
            adminUser.setFirst(firstName);
            adminUser.setLast(lastName);
            adminUser.setEmail(adminUsername);
            adminUser.setContact(adminUser.getContact() == null ? "" : adminUser.getContact());
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setEnabled(true);
            adminUser.setActive(true);
            adminUser.setRole("ROLE_ADMIN");

            userRepository.save(adminUser);
        };
    }
}
