package com.Dk3.Cars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
@org.springframework.scheduling.annotation.Async
@Service
public class EmailService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;


    @Async
    public void sendVerificationEmail(String to, String link) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("DK3 Cars <dk3cars@gmail.com>");
        message.setTo(to);
        message.setSubject("Verify your DK3 Cars account");
        message.setText("""
        Welcome to DK3 Cars 🚗

        confirm your account:
        """ + link + """

        If you did not create this account, ignore this email.
        """);

        try {
            mailSender.send(message);
            logger.info("Verification email sent to {}", to);
        } catch (Exception e) {
            // Log the error but don't rethrow to avoid crashing async processing
            logger.error("Failed to send verification email to {}: {}", to, e.getMessage(), e);
        }
    }

} 
