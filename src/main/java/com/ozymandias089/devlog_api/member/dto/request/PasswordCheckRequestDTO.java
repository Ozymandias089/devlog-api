package com.ozymandias089.devlog_api.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class PasswordCheckRequestDTO {
    @NotBlank @Getter
    private String password;
}
