package com.ozymandias089.devlog_api.post.repository;

import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import com.ozymandias089.devlog_api.post.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    /* ==================== 기본 조회 ==================== */

    /**
     * 특정 작성자의 게시글을 페이지로 조회합니다.
     *
     * @param author   작성자 엔티티
     * @param pageable 페이지 정보(정렬/페이지 크기)
     */
    Page<PostEntity> findByAuthor(MemberEntity author, Pageable pageable);

    /**
     * 작성자 UUID로 게시글을 페이지 조회합니다.
     * (연관 엔티티를 직접 만들지 않고도 접근 가능한 파생 쿼리)
     *
     * @param authorUuid 작성자 UUID
     * @param pageable   페이지 정보
     */
    Page<PostEntity> findByAuthorUuid(UUID authorUuid, Pageable pageable);

    /**
     * 작성자 UUID 기준 게시글 수를 반환합니다.
     *
     * @param authorUuid 작성자 UUID
     */
    long countByAuthorUuid(UUID authorUuid);

    /**
     * 특정 게시글의 조회수를 1 증가시킵니다.
     * (DB에서 원자적으로 증가)
     *
     * @param postId 게시글 PK
     * @return 업데이트된 행 수(0 또는 1)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PostEntity p set p.viewCount = p.viewCount + 1 where p.id = :postId")
    int increaseViewCount(@Param("postId") Long postId);

    /* ==================== 리스트/요약용 프로젝션 ==================== */

    /**
     * 리스트 화면에서 필요한 최소 필드만 조회하는 프로젝션입니다.
     * 엔티티 전체 로딩을 피해서 성능을 최적화합니다.
     */
    interface PostSummary {
        Long getId();
        String getTitle();
        Long getViewCount();
        Instant getCreatedAt();
    }

    /**
     * 작성자 UUID 기준으로 요약 정보만 페이지 조회합니다.
     * 정렬은 {@code pageable}의 Sort(예: createdAt desc, viewCount desc 등)를 사용하세요.
     *
     * @param authorUuid 작성자 UUID
     * @param pageable   페이지 정보
     */
    Page<PostSummary> findPostSummariesByAuthorUuid(UUID authorUuid, Pageable pageable);

    /* ==================== 유틸 ==================== */

    /**
     * 해당 작성자가 소유한 게시글인지 확인할 때 사용합니다.
     *
     * @param id         게시글 PK
     * @param authorUuid 작성자 UUID
     */
    boolean existsByIdAndAuthorUuid(Long id, UUID authorUuid);

    boolean existsByAuthorAndSlug(MemberEntity author, String slug);
    Optional<PostEntity> findBySlug(String slug);                      // 전역 유일일 경우
    Optional<PostEntity> findByAuthorUuidAndSlug(UUID author, String slug); // 작성자별 유일일 경우
}
