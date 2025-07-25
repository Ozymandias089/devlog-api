package com.ozymandias089.devlog_api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SignupRequestDTO {
    @Email @NotBlank
    private String email;
    @NotBlank
    private String password;

    @Builder
    public SignupRequestDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
