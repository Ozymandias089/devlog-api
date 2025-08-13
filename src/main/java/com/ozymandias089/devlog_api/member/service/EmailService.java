package com.ozymandias089.devlog_api.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending application-related emails.
 * <p>
 * This service is responsible for composing and sending emails to users.
 * Currently supports:
 * <ul>
 *     <li>Sending password reset links</li>
 * </ul>
 * </p>
 *
 * @author Younghoon Choi
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    /**
     * Sends a password reset email to the specified recipient.
     * <p>
     * The email contains a reset URL that the user can click to reset their password.
     * This method uses {@link JavaMailSender} to send a simple text-based email.
     * </p>
     *
     * @param toEmail the recipient's email address
     * @param resetURL the URL that allows the user to reset their password
     */
    public void sendPasswordResetEmail(String toEmail, String resetURL) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetURL);
        javaMailSender.send(message);
    }
}
