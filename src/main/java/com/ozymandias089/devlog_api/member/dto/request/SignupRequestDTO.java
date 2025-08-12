package com.ozymandias089.devlog_api.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

public class SignupRequestDTO {
    @Email @NotBlank @Getter
    private String email;
    @NotBlank @Getter
    private String password;
}
