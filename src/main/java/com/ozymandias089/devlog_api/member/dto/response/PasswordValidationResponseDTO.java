package com.ozymandias089.devlog_api.member.dto.response;

import lombok.Builder;

import java.util.List;

public class PasswordValidationResponseDTO {
    private boolean isValid;
    private List<String> errors;

    @Builder
    public PasswordValidationResponseDTO(boolean isValid, List<String> errors) {
        this.isValid = isValid;
        this.errors = errors;
    }
}
