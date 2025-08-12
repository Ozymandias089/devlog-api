package com.ozymandias089.devlog_api.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class PasswordResetConfirmRequestDTO {
    @NotBlank
    @Getter
    private String resetToken;

    @NotBlank
    @Getter
    private String newPassword;

    @Builder
    public PasswordResetConfirmRequestDTO(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }
}
