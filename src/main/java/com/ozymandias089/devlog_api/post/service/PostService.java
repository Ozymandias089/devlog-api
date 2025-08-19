package com.ozymandias089.devlog_api.post.service;

import com.ozymandias089.devlog_api.global.exception.ForbiddenActionException;
import com.ozymandias089.devlog_api.global.exception.InvalidCredentialsException;
import com.ozymandias089.devlog_api.global.exception.PostNotFoundException;
import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import com.ozymandias089.devlog_api.member.repository.MemberRepository;
import com.ozymandias089.devlog_api.post.dto.PostSummaryDTO;
import com.ozymandias089.devlog_api.post.dto.request.CreatePostRequestDTO;
import com.ozymandias089.devlog_api.post.dto.request.UpdatePostRequestDTO;
import com.ozymandias089.devlog_api.post.dto.response.GetDetailedPostResponseDTO;
import com.ozymandias089.devlog_api.post.dto.response.GetPostListResponseDTO;
import com.ozymandias089.devlog_api.post.entity.PostEntity;
import com.ozymandias089.devlog_api.post.provider.PostMapper;
import com.ozymandias089.devlog_api.post.provider.SlugProvider;
import com.ozymandias089.devlog_api.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 게시글(Post) 도메인의 애플리케이션 서비스입니다.
 *
 * <p>
 * 생성/조회 등 유스케이스를 제공하며, 트랜잭션 경계를 관리합니다.
 * 목록 조회는 인터페이스 프로젝션을 사용해 필요한 필드만 조회하여 성능을 최적화합니다.
 * 상세 조회는 조회수를 원자적으로 증가시킨 뒤 작성자 정보를 포함해 반환합니다.
 * </p>
 *
 * <h3>트랜잭션 정책</h3>
 * <ul>
 *   <li>쓰기 작업: {@link Transactional} (기본)</li>
 *   <li>읽기 작업: {@link Transactional#readOnly()} = true</li>
 * </ul>
 *
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final SlugProvider slugProvider;

    /**
     * 새 게시글을 생성하고 최종 확정된 슬러그를 반환합니다.
     *
     * <p>
     * 1) <code>uuid</code>로 작성자 식별 후 없으면 {@link InvalidCredentialsException}.<br>
     * 2) 제목을 기반으로 고유한 슬러그를 생성합니다.<br>
     * 3) 저장 시 유니크 제약으로 인한 {@link DataIntegrityViolationException}이 발생하면
     *    슬러그를 재생성하여 한 번 더 저장을 시도합니다.<br>
     * 4) 성공 시 최종 슬러그를 반환합니다.
     * </p>
     *
     * @param uuid 인증된 사용자의 UUID(문자열)
     * @param createPostRequestDTO 제목/내용을 담은 요청 DTO
     * @return 생성된 게시글의 최종 슬러그
     * @throws InvalidCredentialsException 사용자 UUID에 해당하는 멤버가 없을 때
     * @throws DataIntegrityViolationException 드물게 재시도 후에도 슬러그 충돌이 지속되는 경우 전파될 수 있음
     * @implNote 전역 유일 슬러그 정책을 가정합니다(스키마에 <code>unique index (slug)</code> 권장).
     * @since 1.0
     */
    @Transactional
    public String createPost(String uuid, CreatePostRequestDTO createPostRequestDTO) {
        MemberEntity member = memberRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new InvalidCredentialsException("No member found with the provided Token"));

        String slug = slugProvider.generateUniqueSlug(member, createPostRequestDTO.title());

        PostEntity post = PostMapper.toPostEntity(member, createPostRequestDTO.title(), createPostRequestDTO.content(), slug);

        try {
            postRepository.save(post);
            return slug;
        } catch (DataIntegrityViolationException ex) {
            String fallback = slugProvider.generateUniqueSlug(member, createPostRequestDTO.title());
            post = PostMapper.toPostEntity(member, createPostRequestDTO.title(), createPostRequestDTO.content(), fallback);
            postRepository.save(post);
            return fallback;
        }
    }

    /**
     * 게시글 목록을 페이지네이션하여 조회합니다(최신순).
     *
     * <p>
     * 페이지 크기는 1~20 범위로 캡 처리되며, 정렬은 <code>createdAt DESC, id DESC</code>로 안정화합니다.
     * 인터페이스 프로젝션({@link PostRepository.ListRow})으로 필요한 필드만 조회한 뒤
     * {@link PostSummaryDTO} 리스트로 매핑하여 응답 DTO에 담아 반환합니다.
     * </p>
     *
     * @param page 0부터 시작하는 페이지 인덱스(기본값 0)
     * @param size 페이지 크기(기본값 20, 최대 20)
     * @return 목록 및 페이지 정보를 담은 {@link GetPostListResponseDTO}
     * @since 1.0
     */
    @Transactional(readOnly = true)
    public GetPostListResponseDTO getPostList(int page, int size) {
        var sort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        int capped = Math.min(Math.max(size, 1), 20); // size 최대 20
        var pageable = PageRequest.of(page, capped, sort);

        Page<PostRepository.ListRow> pageData = postRepository.findAllProjectedBy(pageable);
        List<PostSummaryDTO> posts = PostMapper.toPostSummaryDTOs(pageData);

        return new GetPostListResponseDTO(
                posts,
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.hasNext(),
                pageData.hasPrevious()
        );
    }

    /**
     * 슬러그로 단일 게시글 상세를 조회합니다. (조회수 1 증가 후 조회)
     *
     * <p>
     * 1) 먼저 벌크 업데이트로 <code>viewCount</code>를 +1 합니다(영속성 컨텍스트 변경 감지/감사 미발생).<br>
     * 2) 영향받은 행이 없으면 {@link PostNotFoundException}.<br>
     * 3) 작성자 정보를 함께 가져오는 쿼리로 상세를 조회하여 DTO로 반환합니다.
     * </p>
     *
     * <p><em>NOTE:</em> 조회수 증가는 벌크 업데이트이므로 <code>updatedAt</code> 등 감사 필드가 변경되지 않습니다.</p>
     *
     * @param slug 전역 유일 슬러그
     * @return 제목, 작성자(UUID/username), 조회수, 생성일, 본문을 포함한 {@link GetDetailedPostResponseDTO}
     * @throws PostNotFoundException 주어진 슬러그의 게시글이 없을 때
     * @implNote <code>@Modifying(clearAutomatically = true, flushAutomatically = true)</code>로
     *           벌크 업데이트 직후의 조회가 최신 상태를 읽도록 안전장치를 두었습니다.
     * @since 1.0
     */
    @Transactional
    public GetDetailedPostResponseDTO getPostDetailed(String slug) {
        // 1) 조회수 +1 (감사 X → updatedAt 그대로)
        int rows = postRepository.incrementViewCountBySlug(slug);
        if (rows == 0) throw new PostNotFoundException(slug);

        PostEntity post = postRepository.findBySlugWithAuthor(slug).orElseThrow(() -> new PostNotFoundException(slug));

        return new GetDetailedPostResponseDTO(
                post.getTitle(),
                post.getAuthor().getUuid(),
                post.getAuthor().getUsername(),
                post.getViewCount(),
                post.getCreatedAt(),
                post.getContent()
        );
    }

    /**
     * 슬러그로 식별되는 게시글을 부분 수정(PATCH)합니다.
     *
     * <p><strong>동작 순서</strong></p>
     * <ol>
     *   <li>슬러그로 대상 {@link PostEntity}를 조회합니다. 없으면 {@link com.ozymandias089.devlog_api.global.exception.PostNotFoundException}.</li>
     *   <li>요청자의 UUID와 게시글 작성자 UUID를 비교해 권한을 검증합니다. 불일치 시 {@link com.ozymandias089.devlog_api.global.exception.ForbiddenActionException}.</li>
     *   <li>{@link UpdatePostRequestDTO}의 <code>title</code>/<code>content</code>가
     *       <em>null/blank가 아니면서</em> 기존 값과 다를 때만 반영합니다.</li>
     * </ol>
     *
     * <p>
     * 이 메서드는 트랜잭션 안에서 JPA 더티 체킹에 의해 변경 사항이 커밋 시점에 자동 반영됩니다.
     * 슬러그는 정책상 불변이므로 저장된 <em>정본(canonical)</em> 슬러그를 그대로 반환합니다.
     * </p>
     *
     * @param uuid     인증 사용자의 UUID 문자열(예: JWT subject)
     * @param slug     수정 대상 게시글의 슬러그
     * @param requestDTO 수정할 제목/내용을 담은 DTO(필드가 null/blank면 해당 항목은 미수정)
     * @return 저장된 게시글의 정본(canonical) 슬러그
     *
     * @throws com.ozymandias089.devlog_api.global.exception.PostNotFoundException
     *         슬러그에 해당하는 게시글이 존재하지 않는 경우
     * @throws com.ozymandias089.devlog_api.global.exception.ForbiddenActionException
     *         요청자(UUID)가 게시글 작성자가 아닌 경우
     *
     * @implNote 동시 수정 충돌 방지를 위해 필요 시 {@link jakarta.persistence.Version @Version} 기반 낙관적 락 도입을 고려하세요.
     *           (예: 요청/응답에 version 포함 후 불일치 시 409/412 처리)
     * @since 1.0
     */
    @Transactional
    public String updatePost(String uuid, String slug, UpdatePostRequestDTO requestDTO) {
        // 1. 슬러그로 포스트를 검색해온다.
        PostEntity targetPost = postRepository.findBySlugWithAuthor(slug).orElseThrow(() -> new PostNotFoundException(slug));
        // 2. 토큰 파라미터로 받은 uuid와 슬러그로 검색해온 포스트의 uuid를 비교해 유효성을 검증한다.
        if (!targetPost.getAuthor().getUuid().equals(UUID.fromString(uuid))) throw new ForbiddenActionException("Unauthorized Action.");

        // 3. dto의 제목, 내용을 반영한다.
        if (requestDTO.title() != null &&
                !requestDTO.title().isBlank() &&
                !requestDTO.title().equals(targetPost.getTitle())
        ) targetPost.updateTitle(requestDTO.title().trim());

        if (requestDTO.content() != null &&
                !requestDTO.content().isBlank() &&
                !requestDTO.content().equals(targetPost.getContent()))
            targetPost.updateContent(requestDTO.content());

        // 4. 정본 슬러그 반환 (불변 정책)
        return targetPost.getSlug();
    }

    /**
     * 슬러그로 식별되는 게시글을 삭제합니다.
     *
     * <p><strong>동작 순서</strong></p>
     * <ol>
     *   <li>슬러그로 대상 {@link PostEntity}를 조회합니다. 없으면
     *       {@link com.ozymandias089.devlog_api.global.exception.PostNotFoundException} 발생.</li>
     *   <li>요청자 UUID와 게시글 작성자 UUID가 일치하는지 검증합니다. 불일치 시
     *       {@link com.ozymandias089.devlog_api.global.exception.ForbiddenActionException} 발생.</li>
     *   <li>검증을 통과하면 해당 게시글을 삭제합니다.</li>
     * </ol>
     *
     * <p>트랜잭션 안에서 실행되며, 예외 발생 시 롤백됩니다.</p>
     *
     * @param uuid 요청자(UUID 문자열, 보통 JWT subject)
     * @param slug 삭제 대상 게시글의 전역 유일 슬러그
     *
     * @throws com.ozymandias089.devlog_api.global.exception.PostNotFoundException
     *         주어진 슬러그의 게시글이 존재하지 않는 경우
     * @throws com.ozymandias089.devlog_api.global.exception.ForbiddenActionException
     *         요청자가 게시글 작성자가 아니어서 삭제 권한이 없는 경우
     *
     * @implNote 댓글/좋아요 등 연관 데이터가 있다면 FK 제약, JPA cascade(orphanRemoval), 소프트 딜리트 전략을
     *           사전에 정렬해야 합니다. FK 위반을 즉시 드러내고 싶으면 필요에 따라 {@code entityManager.flush()}를 호출하세요.
     * @since 1.0
     */
    @Transactional
    public void deletePost(String uuid, String slug) {
        PostEntity post = postRepository.findBySlugWithAuthor(slug).orElseThrow(() -> new PostNotFoundException(slug));
        if (!post.getAuthor().getUuid().equals(UUID.fromString(uuid))) throw new ForbiddenActionException("Action unauthorized.");

        postRepository.delete(post);
    }
}
