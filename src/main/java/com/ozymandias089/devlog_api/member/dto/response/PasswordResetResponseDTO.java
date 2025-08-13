package com.ozymandias089.devlog_api.member.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class PasswordResetResponseDTO {
    @Getter @NotBlank
    private String resetToken;

    @Builder
    public PasswordResetResponseDTO(String resetToken) {
        this.resetToken = resetToken;
    }
}
