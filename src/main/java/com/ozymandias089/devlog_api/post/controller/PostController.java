package com.ozymandias089.devlog_api.post.controller;

import com.ozymandias089.devlog_api.post.dto.request.CreatePostRequestDTO;
import com.ozymandias089.devlog_api.post.dto.request.UpdatePostRequestDTO;
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
import org.springframework.web.util.UriComponentsBuilder;

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

    /**
     * 게시글을 부분 수정(PATCH)하고, 최신 상태의 상세 리소스로 리다이렉트(303 See Other)합니다.
     *
     * <p>
     * - 인증 사용자의 식별자(일반적으로 JWT subject)를 이용해 권한을 검증하고,<br>
     * - 요청 DTO의 값으로 제목/내용을 갱신한 뒤,<br>
     * - 서비스가 반환한 <em>정본(canonical)</em> 슬러그를 사용해 <code>Location</code> 헤더에
     *   <code>/api/posts/{slug}</code> URI를 설정하여 <strong>303 See Other</strong> 응답을 반환합니다.<br>
     *   (응답 바디는 없습니다)
     * </p>
     *
     * <p><strong>주의</strong>: 이 엔드포인트는 <code>produces = "application/json"</code>로 선언되어 있으나,
     * 303 응답에는 바디가 없으므로 실제 페이로드는 전송되지 않습니다. 클라이언트는
     * <code>Location</code> 헤더가 가리키는 URI로 GET을 수행해 최신 리소스를 조회해야 합니다.</p>
     *
     * @param userPrincipal 인증 주체. {@link org.springframework.security.core.annotation.AuthenticationPrincipal}
     *                      에 의해 주입되며, {@link java.nio.file.attribute.UserPrincipal#getName()} 에서
     *                      사용자 UUID 문자열을 가져옵니다.
     * @param slug          경로 변수의 슬러그(업데이트 대상 식별자). 슬러그가 정책상 불변이 아닐 수도 있으므로,
     *                      최종 리다이렉트 URI는 서비스에서 반환한 정본 슬러그를 사용합니다.
     * @param requestDTO    수정할 제목/내용을 담은 요청 본문(검증 적용).
     * @param uriComponentsBuilder 현재 요청 컨텍스트를 기반으로 URI를 조립하기 위한 빌더(스프링이 자동 주입).
     * @return <strong>303 See Other</strong> 와 함께 <code>Location</code> 헤더가 설정된 빈 응답 바디
     *
     * @throws com.ozymandias089.devlog_api.global.exception.PostNotFoundException
     *         주어진 슬러그의 게시글이 존재하지 않는 경우
     * @throws com.ozymandias089.devlog_api.global.exception.InvalidCredentialsException
     *         인증된 사용자가 게시글 작성자가 아닌 등 권한이 없는 경우
     * @implNote 리다이렉트 대상 URI는 <em>정본(canonical)</em> 슬러그로 생성됩니다.
     *           (제목 변경에 따른 슬러그 재산정 정책이 도입되어도 안전)
     * @see com.ozymandias089.devlog_api.post.service.PostService#updatePost(String, String, com.ozymandias089.devlog_api.post.dto.request.UpdatePostRequestDTO)
     */
    @PatchMapping(value = "/{slug}", produces = "application/json")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary = "Updates post", description = "토큰과 슬러그, 제목과 내용이 담긴 DTO를 받아 내용을 수정하고 반환")
    public ResponseEntity<Void> updatePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String slug,
            @RequestBody @Valid UpdatePostRequestDTO requestDTO,
            UriComponentsBuilder uriComponentsBuilder
    ) {
        String canonicalSlug = postService.updatePost(userPrincipal.getName(), slug, requestDTO);
        URI location = uriComponentsBuilder.path("/api/posts/{slug}")
                .buildAndExpand(canonicalSlug)
                .toUri();
        return ResponseEntity.status(303).location(location).build();
    }


    // Delete:
    // - 슬러그와 토큰을 받아 작성자 권한을 검증 후 게시글 삭제 예정
}
