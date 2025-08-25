package com.ozymandias089.devlog_api.post.dto.response;

import com.ozymandias089.devlog_api.post.dto.PostSummaryDTO;
import lombok.Builder;

import java.util.List;

public class GetPostListResponseDTO{
    List<PostSummaryDTO> posts;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean hasNext;
    boolean hasPrev;

    @Builder
    public GetPostListResponseDTO(List<PostSummaryDTO> posts, int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrev) {
        this.posts = posts;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
    }
}
