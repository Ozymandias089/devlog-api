package com.ozymandias089.devlog_api.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequestDTO(
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 20_000) String content
) {}
