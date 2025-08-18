package com.ozymandias089.devlog_api.post.dto.response;

import com.ozymandias089.devlog_api.post.dto.PostSummaryDTO;

import java.util.List;

public record GetPostListResponseDTO(
        List<PostSummaryDTO> posts,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrev
) {}
