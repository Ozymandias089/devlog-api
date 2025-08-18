package com.ozymandias089.devlog_api.post.dto;

import java.time.Instant;
import java.util.UUID;

public record PostSummaryDTO(
        String title,
        String slug,
        UUID authorUuid,
        String authorUsername,
        long viewCount,
        Instant createdAt
) {}
