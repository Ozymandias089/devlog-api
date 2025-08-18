package com.ozymandias089.devlog_api.post.dto.response;

import com.ozymandias089.devlog_api.post.dto.PostDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record getPostListDTO(@NotBlank @Size(max = 20) PostDTO[] post) {}
