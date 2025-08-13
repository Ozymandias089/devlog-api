package com.ozymandias089.devlog_api.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class UsernameUpdateRequestDTO {
    @NotBlank @Getter
    private String newUsername;
}
