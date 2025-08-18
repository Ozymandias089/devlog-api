package com.ozymandias089.devlog_api.post.controller;

import com.ozymandias089.devlog_api.post.dto.request.CreatePostRequestDTO;
import com.ozymandias089.devlog_api.post.dto.response.GetDetailedPostResponseDTO;
import com.ozymandias089.devlog_api.post.dto.response.GetPostListResponseDTO;
import com.ozymandias089.devlog_api.post.dto.response.PostCreateResponseDTO;
import com.ozymandias089.devlog_api.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.file.attribute.UserPrincipal;

/**
 * 게시글(Post) 관리용 REST 컨트롤러입니다.
 *
 * <p><strong>Base Path:</strong> <code>/api/posts</code></p>
 * <p>요청/응답은 모두 JSON을 사용합니다.</p>
 *
 * <h3>보안</h3>
 * 인증이 필요한 엔드포인트는 OpenAPI {@link SecurityRequirement} 어노테이션으로 표시되어 있으며,
 * Bearer(JWT) 토큰을 사용합니다.
 *
 * <h3>응답 코드 일반 가이드</h3>
 * <ul>
 *     <li><strong>200 OK</strong> — 정상 조회</li>
 *     <li><strong>201 Created</strong> — 리소스 생성 완료</li>
 *     <li><strong>400 Bad Request</strong> — 유효성 검증 실패 등 잘못된 요청</li>
 *     <li><strong>401/403</strong> — 인증/권한 실패</li>
 *     <li><strong>404 Not Found</strong> — 대상 리소스 없음</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "Post management APIs")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     * 새 게시글을 생성합니다.
     *
     * <p>인증된 사용자의 식별자(일반적으로 JWT subject)를 활용하여 게시글을 저장하고,
     * 생성된 리소스의 위치를 <code>Location</code> 헤더(<code>/api/posts/{slug}</code>)로 반환합니다.</p>
     *
     * @param principal 인증 주체(사용자 식별자). {@link AuthenticationPrincipal}에서 주입됩니다.
     *                  <em>주의:</em> 구현에 따라 Principal의 타입/이름은 달라질 수 있습니다.
     * @param requestDTO 생성할 게시글의 제목/내용 요청 본문 (검증 적용)
     * @return <strong>201 Created</strong> 와 함께 본문에 생성된 slug를 담은 {@link PostCreateResponseDTO},
     *         그리고 <code>Location</code> 헤더를 포함합니다.
     * @since 1.0
     */
    @PostMapping(value = "/create")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Creates a new Post", description = "토큰과 작성할 내용을 받아 엔티티에 저장합니다.")
    public ResponseEntity<PostCreateResponseDTO> createNewPost(@AuthenticationPrincipal UserPrincipal principal, @RequestBody @Valid CreatePostRequestDTO requestDTO) {
        String slug = postService.createPost(principal.getName(), requestDTO);
        return ResponseEntity.created(URI.create("/api/posts/" + slug)).body(new PostCreateResponseDTO(slug));
    }

    /**
     * 게시글 목록을 페이지네이션하여 조회합니다.
     *
     * <p>최대 페이지 크기는 20으로 캡(cap) 처리됩니다. 정렬은 생성일 내림차순(최신순)입니다.</p>
     *
     * @param page 0부터 시작하는 페이지 인덱스(기본값 0)
     * @param size 페이지 크기(기본값 20, 서비스 레벨에서 1~20으로 제한)
     * @return <strong>200 OK</strong> 와 함께 목록/페이지 정보를 담은 {@link GetPostListResponseDTO}
     * @since 1.0
     */
    @GetMapping(value = "/post-list", produces = "application/json")
    @Operation(summary = "Get Post list", description = "Get List of posts. max 20")
    public ResponseEntity<GetPostListResponseDTO> getPostList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(postService.getPostList(page, size));
    }

    /**
     * 단일 게시글 상세를 슬러그로 조회합니다.
     *
     * <p>이 호출은 조회수를 원자적으로 1 증가시킨 뒤, 작성자 정보를 포함한 상세를 반환합니다.</p>
     *
     * @param slug 전역 유일 슬러그
     * @return <strong>200 OK</strong> 와 함께 상세 정보를 담은 {@link GetDetailedPostResponseDTO}
     * @implNote 서비스 레이어에서 먼저 조회수를 증가시키고, 이어서 작성자까지 fetch한 엔티티를 조회합니다.
     * @since 1.0
     */
    @GetMapping(value = "/{slug}", produces = "application/json")
    @Operation(summary = "Read a Post", description = "Read a post. Query with slugs")
    public ResponseEntity<GetDetailedPostResponseDTO> getPostDetails(@PathVariable @Valid String slug) {
        return ResponseEntity.ok(postService.getPostDetailed(slug));
    }

    // Update:
    // - 슬러그와 토큰을 받아 작성자 권한을 검증 후 제목/내용 수정 예정

    // Delete:
    // - 슬러그와 토큰을 받아 작성자 권한을 검증 후 게시글 삭제 예정
}
