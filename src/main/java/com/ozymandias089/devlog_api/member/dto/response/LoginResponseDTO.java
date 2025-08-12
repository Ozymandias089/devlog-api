package com.ozymandias089.devlog_api.member.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class LoginResponseDTO {
    @NotBlank
    private String accessToken;
    @NotBlank
    private String refreshToken;

    @Builder
    public LoginResponseDTO(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
