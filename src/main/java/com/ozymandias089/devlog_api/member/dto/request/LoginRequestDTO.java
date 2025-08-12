package com.ozymandias089.devlog_api.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class LoginRequestDTO {
    @Getter
    @Email
    @NotBlank
    private String email;

    @Getter
    @NotBlank
    private String password;
}
