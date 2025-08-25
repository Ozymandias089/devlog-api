package com.ozymandias089.devlog_api.post.dto.response;

import lombok.Builder;

public class PostCreateResponseDTO {
    String slug;

    @Builder
    public PostCreateResponseDTO(String slug) {
        this.slug = slug;
    }
}
