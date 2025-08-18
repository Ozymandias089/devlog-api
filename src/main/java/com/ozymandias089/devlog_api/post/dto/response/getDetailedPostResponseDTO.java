package com.ozymandias089.devlog_api.post.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record getDetailedPostResponseDTO(
        @NotBlank @Size(max = 150) String title,
        @NotBlank String uuid,
        @NotBlank  String username,
        @NotBlank Long viewCount,
        @NotBlank Instant createdAt,
        @NotBlank String content
) {}
