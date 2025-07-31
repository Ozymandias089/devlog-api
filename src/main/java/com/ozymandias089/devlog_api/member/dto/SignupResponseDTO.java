package com.ozymandias089.devlog_api.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SignupResponseDTO {
    private UUID uuid;
    private String email;
    private String username;
    private String accessToken;
    private String refreshToken;

    @Builder
    public SignupResponseDTO(UUID uuid, String email, String username, String accessToken, String refreshToken) {
        this.uuid = uuid;
        this.email = email;
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}
