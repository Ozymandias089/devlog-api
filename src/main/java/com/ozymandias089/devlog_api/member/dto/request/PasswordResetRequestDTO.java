package com.ozymandias089.devlog_api.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class PasswordResetRequestDTO {
    @Getter
    @Email
    private String email;

    @Builder
    public PasswordResetRequestDTO(String email) {
        this.email = email;
    }
}
