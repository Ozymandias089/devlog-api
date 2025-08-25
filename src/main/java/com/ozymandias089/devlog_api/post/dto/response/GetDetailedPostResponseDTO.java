package com.ozymandias089.devlog_api.post.dto.response;

import lombok.Builder;
import java.time.Instant;

public class GetDetailedPostResponseDTO{
    String title;
    String authorUuid;
    String authorUsername;
    long viewCount;
    Instant createdAt;
    String content;

    @Builder
    public GetDetailedPostResponseDTO (String title, String authorUuid, String authorUsername, long viewCount, Instant createdAt, String content) {
        this.title = title;
        this.authorUuid= authorUuid;
        this.authorUsername = authorUsername;
        this.viewCount = viewCount;
        this.createdAt= createdAt;
        this.content = content;
    }
}
