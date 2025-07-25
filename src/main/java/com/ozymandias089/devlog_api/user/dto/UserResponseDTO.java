package com.ozymandias089.devlog_api.user.dto;

import lombok.Builder;

import java.util.UUID;

public class UserResponseDTO {
    private UUID uuid;
    private String email;
    private String username;

    @Builder
    public UserResponseDTO(UUID uuid, String email, String username) {
        this.uuid = uuid;
        this.email = email;
        this.username = username;
    }
}
