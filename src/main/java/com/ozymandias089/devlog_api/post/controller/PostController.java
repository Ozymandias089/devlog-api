package com.ozymandias089.devlog_api.post.controller;

import com.ozymandias089.devlog_api.post.dto.request.CreatePostRequestDTO;
import com.ozymandias089.devlog_api.post.dto.response.PostCreateResponseDTO;
import com.ozymandias089.devlog_api.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.file.attribute.UserPrincipal;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "Post management APIs")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    // Create
    @PostMapping(value = "/create")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creates a new Post", description = "토큰과 작성할 내용을 받아 엔티티에 저장합니다.")
    public ResponseEntity<PostCreateResponseDTO> createNewPost(@AuthenticationPrincipal UserPrincipal principal, @RequestBody @Valid CreatePostRequestDTO requestDTO) {
        String slug = postService.createPost(principal.getName(), requestDTO);
        return ResponseEntity.created(URI.create("/api/posts/" + slug)).body(new PostCreateResponseDTO(slug));
    }

    // Read
    // Get detailed info of one post. slug, title, content, createdAt, member username, uuid?, viewCount. Header is slug
    // Get lists of posts. 20 max, {title, author.username, viewcount, createdAt}. increment by createdAt reversed

    // Update
    // Update. parameter is Slug and token. check token name with post author's uuid. Maybe consider using a session.

    // Delete
    // Delete post. validate token uuid with author's uuid.
}
