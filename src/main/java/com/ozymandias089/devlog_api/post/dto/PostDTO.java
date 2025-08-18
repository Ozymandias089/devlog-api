package com.ozymandias089.devlog_api.post.dto;

import jakarta.validation.constraints.NotBlank;

public record PostDTO(
        @NotBlank String title,
        @NotBlank String uuid,
        @NotBlank String username,
        @NotBlank String viewCount,
        @NotBlank String createdAt
) {}
