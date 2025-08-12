package com.ozymandias089.devlog_api.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class PasswordValidationRequestDTO {
    @NotBlank
    @Getter
    private String password;
}
