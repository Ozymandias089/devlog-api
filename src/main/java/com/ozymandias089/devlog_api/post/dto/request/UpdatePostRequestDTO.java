package com.ozymandias089.devlog_api.post.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * 게시글 부분 수정(PATCH) 요청 DTO입니다.
 *
 * <p>
 * 두 필드는 모두 선택(optional)입니다. {@code null}이 전달되면 해당 항목은 <b>수정하지 않습니다</b>.
 * 빈 문자열/공백만 있는 값은 서비스 레이어에서 무시되도록 처리되어 있습니다.
 * </p>
 *
 * <h3>검증 규칙</h3>
 * <ul>
 *   <li><b>title</b>: 최대 150자</li>
 *   <li><b>content</b>: 최대 20,000자</li>
 * </ul>
 *
 * <h3>예시</h3>
 * <pre>{@code
 * // 제목만 수정
 * {
 *   "title": "새 제목"
 * }
 *
 * // 본문만 수정
 * {
 *   "content": "새 본문 내용..."
 * }
 *
 * // 둘 다 수정
 * {
 *   "title": "새 제목",
 *   "content": "새 본문 내용..."
 * }
 * }</pre>
 *
 * @implNote 문자열 길이만 제한합니다. 공백/빈 문자열은 서비스에서 필터링하여 미수정으로 처리합니다.
 *           부분 수정 의도(PATCH)를 유지하기 위해 {@code @NotBlank}는 사용하지 않습니다.
 * @since 1.0
 */
public class UpdatePostRequestDTO {
        @Getter @Size(max = 150) String title;
        @Getter @Size(max = 20_000) String content;
}
