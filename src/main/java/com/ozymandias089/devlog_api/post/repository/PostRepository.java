package com.ozymandias089.devlog_api.post.repository;

import com.ozymandias089.devlog_api.post.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link PostEntity}에 대한 JPA 리포지토리입니다.
 *
 * <p><strong>정책:</strong> 슬러그(slug)는 전역 유일을 가정합니다.
 * 스키마에서 <code>unique index (slug)</code> 구성을 권장합니다.</p>
 *
 * <h3>쿼리 설계</h3>
 * <ul>
 *   <li>목록 조회는 인터페이스 프로젝션({@link ListRow})을 사용해 필요한 필드만 읽어 성능을 최적화합니다.</li>
 *   <li>상세 조회는 작성자(author)를 즉시 로딩(fetch join)하여 N+1을 방지합니다.</li>
 *   <li>조회수 증가는 벌크 업데이트로 원자적으로 처리합니다(감사/감지 미발생).</li>
 * </ul>
 *
 * @since 1.0
 */
@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    /* ==================== 기본 조회 ==================== */

    /**
     * 게시글 목록 화면에서 사용하는 최소 필드 전용 프로젝션입니다.
     *
     * <p>중첩 인터페이스 {@link AuthorView}를 통해 작성자 식별 정보만 노출합니다.</p>
     */
    interface ListRow {
        /** 게시글 제목 */
        String getTitle();
        /** 전역 유일 슬러그 */
        String getSlug();
        /** 조회수 */
        Long getViewCount();
        /** 생성 시각 */
        Instant getCreatedAt();
        /** 작성자 요약 뷰 */
        AuthorView getAuthor();

        /**
         * 작성자 요약 프로젝션입니다.
         */
        interface AuthorView {
            /** 작성자 UUID */
            UUID getUuid();
            /** 작성자 표시 이름 */
            String getUsername();
        }
    }

    /* ==================== 상세 조회 ==================== */

    /**
     * 슬러그로 단일 게시글을 조회하며, 작성자를 fetch join으로 함께 로딩합니다.
     *
     * @param slug 전역 유일 슬러그
     * @return 게시글 엔티티(작성자 포함). 없으면 {@link Optional#empty()}
     * @implNote JPQL의 <code>join fetch</code>로 author 연관을 즉시 로딩하여 N+1 문제를 예방합니다.
     */
    @Query("""
      select p
      from PostEntity p
      join fetch p.author
      where p.slug = :slug
    """)
    Optional<PostEntity> findBySlugWithAuthor(@Param("slug") String slug);

    /* ==================== 조회수 증가 ==================== */

    /**
     * 슬러그 기준으로 조회수를 1 증가시킵니다(원자적 증가).
     *
     * <p>벌크 업데이트이므로 영속성 컨텍스트의 변경 감지나 감사 필드(updatedAt 등)는 트리거되지 않습니다.</p>
     *
     * @param slug 전역 유일 슬러그
     * @return 수정된 행 수(성공 시 1, 없으면 0)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PostEntity p set p.viewCount = p.viewCount + 1 where p.slug = :slug")
    int incrementViewCountBySlug(@Param("slug") String slug);

    /* ==================== 목록 조회 ==================== */

    /**
     * 페이지네이션 가능한 게시글 목록을 프로젝션으로 조회합니다.
     *
     * <p>
     * 정렬은 {@link Pageable#getSort()}를 그대로 사용합니다.<br>
     * {@link EntityGraph}로 author 연관을 미리 로딩해 프로젝션에서 작성자 필드를 접근할 때
     * 추가 쿼리가 발생하지 않도록 합니다.
     * </p>
     *
     * @param pageable 페이지/정렬 정보
     * @return 프로젝션 기반 페이지 결과
     */
    @EntityGraph(attributePaths = "author")
    Page<ListRow> findAllProjectedBy(Pageable pageable);

    /* ==================== 유틸 ==================== */

    /**
     * 주어진 슬러그가 존재하는지 여부를 반환합니다.
     *
     * @param slug 전역 유일 슬러그
     * @return 존재하면 true
     */
    boolean existsBySlug(String slug); // 전역 유일 정책이면 유지
}
