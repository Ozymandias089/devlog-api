package com.ozymandias089.devlog_api.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class CreatePostRequestDTO {
    @Getter @NotBlank @Size(max = 150) String title;
    @Getter @NotBlank @Size(max = 20_000) String content;
}