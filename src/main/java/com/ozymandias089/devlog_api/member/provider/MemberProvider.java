package com.ozymandias089.devlog_api.member.provider;

import com.ozymandias089.devlog_api.global.util.RegexPatterns;
import com.ozymandias089.devlog_api.member.PasswordValidationResult;
import com.ozymandias089.devlog_api.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberProvider {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Validates the given plain-text password against a predefined set of security rules.
     * <p>
     * This method enforces the following rules:
     * <ul>
     *   <li>Must not be {@code null} or blank</li>
     *   <li>Must be at least 8 characters long</li>
     *   <li>Must contain at least one uppercase letter ({@code A-Z})</li>
     *   <li>Must contain at least one lowercase letter ({@code a-z})</li>
     *   <li>Must contain at least one digit ({@code 0-9})</li>
     *   <li>Must contain at least one special character from the set {@code !@#$%^&*()}</li>
     * </ul>
     * Any rule violations are collected into an error list.
     * </p>
     *
     * @param password the plain-text password to validate
     * @return a {@link PasswordValidationResult} containing:
     *         <ul>
     *           <li>{@code validity} - {@code true} if the password passes all rules, {@code false} otherwise</li>
     *           <li>{@code errors} - a list of rule violation messages; empty if valid</li>
     *         </ul>
     */
    public PasswordValidationResult passwordValidator(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isBlank()) {
            errors.add("Password cannot be empty.");
        } else {
            if (password.length() < 8) errors.add("Password must be at least 8 characters long.");
            if (!password.matches(".*[A-Z].*")) errors.add("Password must contain at least one uppercase letter.");
            if (!password.matches(".*[a-z].*")) errors.add("Password must contain at least one lowercase letter.");
            if (!password.matches(".*\\d.*")) errors.add("Password must contain at least one digit.");
            if (!password.matches(".*[!@#$%^&*()].*")) errors.add("Password must contain at least one special character (!@#$%^&*()).");
        }

        return new PasswordValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Generates a unique username in the format "User-xxxxxx", where 'xxxxxx' is a zero-padded
     * random 6-digit number. The method ensures that the generated username does not already
     * exist in the repository.
     *
     * @return a unique username string
     */
    public String generateUsername() {
        String username;
        do {
            int random = (int) (Math.random() * 1_000_000);
            username = String.format("User-%06d", random);
        } while (memberRepository.findByUsername(username).isPresent());
        return username;
    }

    /**
     * Checks if the given email is valid in format and not already registered.
     *
     * @param email The email address to check
     * @return true if the email format is valid and not already in use; false otherwise
     */
    public boolean isEmailValidAndUnique(String email) {
        if (email == null || email.isBlank()) return false;
        if (!RegexPatterns.EMAIL_REGEX.matcher(email).matches()) return false;
        return !isEmailValidAndUnique(email);
    }

    /**
     * Hashes the given plain text password using the configured PasswordEncoder.
     *
     * @param password      the plain text password to be hashed
     * @return the encoded (hashed) password string
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
