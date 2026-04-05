package com.Dk3.Cars.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@org.springframework.scheduling.annotation.Async
@Service
public class EmailService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Async
    public void sendVerificationEmail(String to, String link) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Verify your DK3 Cars account");
        message.setText(
                """
                            Welcome to DK3 Cars 🚗
                        Create your account to explore top-quality vehicles, enjoy tailored services, and experience excellence in automotive solutions.

                                confirm your account:
                                """
                        + link + """

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

    @Async
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("DK3 Cars Password Reset OTP");
        message.setText("Your OTP is: " + otp + "\nValid for 10 minutes.");
        try {
            mailSender.send(message);
            logger.info("Password reset OTP email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Async
    public void sendBookingConfirmationWithAttachments(String to, String subject, String body,
            Map<String, byte[]> attachments) {
        try {
            var mimeMessage = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            for (Map.Entry<String, byte[]> entry : attachments.entrySet()) {
                helper.addAttachment(entry.getKey(), new ByteArrayResource(entry.getValue()));
            }
            mailSender.send(mimeMessage);
            logger.info("Booking confirmation email sent to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email to {}: {}", to, e.getMessage(), e);
        }
    }
}
