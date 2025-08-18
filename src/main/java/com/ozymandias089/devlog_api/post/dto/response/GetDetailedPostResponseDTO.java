package com.ozymandias089.devlog_api.post.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GetDetailedPostResponseDTO(
        String title,
        UUID authorUuid,
        String authorUsername,
        long viewCount,
        Instant createdAt,
        String content
) {}
