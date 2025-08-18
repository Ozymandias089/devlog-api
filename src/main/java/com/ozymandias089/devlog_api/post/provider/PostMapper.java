package com.ozymandias089.devlog_api.post.provider;

import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import com.ozymandias089.devlog_api.post.dto.PostSummaryDTO;
import com.ozymandias089.devlog_api.post.entity.PostEntity;
import com.ozymandias089.devlog_api.post.repository.PostRepository;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Post 도메인의 엔티티/DTO 매핑 유틸리티입니다.
 *
 * <p>
 * - 서비스/컨트롤러 계층에서 반복되는 변환 로직을 캡슐화합니다.<br>
 * - 정적(static) 메서드만을 제공하며 상태를 가지지 않습니다(스레드 세이프).
 * </p>
 *
 * @since 1.0
 */
public class PostMapper {
    private PostMapper() {}

    /**
     * 주어진 작성자/제목/본문/슬러그로 {@link PostEntity}를 생성합니다.
     *
     * <p>
     * 영속성 컨텍스트에 저장되기 전이므로 ID 등은 설정되지 않습니다.
     * 감사 필드(createdAt/updatedAt)나 기본값(viewCount 등)은 엔티티/감사 설정에 따릅니다.
     * </p>
     *
     * @param member  작성자 엔티티(Null 아님)
     * @param title   게시글 제목
     * @param content 게시글 본문
     * @param slug    전역 유일 슬러그(정책상 유니크 인덱스 권장)
     * @return 빌더로부터 생성된 {@link PostEntity}
     */
    public static PostEntity toPostEntity(MemberEntity member, String title, String content, String slug) {
        return PostEntity.builder()
                .author(member)
                .title(title)
                .content(content)
                .slug(slug)
                .build();
    }

    /**
     * 목록 화면용 인터페이스 프로젝션({@link PostRepository.ListRow}) 페이지를
     * {@link PostSummaryDTO} 리스트로 변환합니다.
     *
     * <p>
     * 성능을 위해 {@link Page#getContent()}로 현재 페이지의 데이터만 매핑합니다.
     * 프로젝션은 필요한 필드만 조회하므로 N+1 없이 가볍게 동작합니다.
     * </p>
     *
     * @param pageData {@link PostRepository.ListRow} 프로젝션 페이지
     * @return 변환된 {@link PostSummaryDTO} 리스트
     */
    public static List<PostSummaryDTO> toPostSummaryDTOs(Page<PostRepository.ListRow> pageData) {
        return pageData.getContent().stream()
                .map(row ->
                    new PostSummaryDTO(
                        row.getTitle(),
                        row.getSlug(),
                        row.getAuthor().getUuid(),
                        row.getAuthor().getUsername(),
                        row.getViewCount(),
                        row.getCreatedAt()
                    )
        ).toList();
    }
}
