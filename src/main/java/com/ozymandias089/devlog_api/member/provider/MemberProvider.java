package com.ozymandias089.devlog_api.member.provider;

import com.ozymandias089.devlog_api.member.PasswordValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MemberProvider {
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
}
