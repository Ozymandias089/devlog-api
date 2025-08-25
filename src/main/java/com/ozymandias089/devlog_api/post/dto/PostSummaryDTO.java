package com.ozymandias089.devlog_api.post.dto;

import lombok.Builder;

import java.time.Instant;

public class PostSummaryDTO{
    String title;
    String slug;
    String authorUuid;
    String authorUsername;
    long viewCount;
    Instant createdAt;

    @Builder
    public PostSummaryDTO(String title, String slug, String authorUuid, String authorUsername, long viewCount, Instant createdAt) {
        this.title = title;
        this.slug = slug;
        this.authorUuid = authorUuid;
        this.authorUsername = authorUsername;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
    }
}
