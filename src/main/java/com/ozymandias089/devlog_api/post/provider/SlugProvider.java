package com.ozymandias089.devlog_api.post.provider;

import com.ozymandias089.devlog_api.global.util.SlugUtil;
import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import com.ozymandias089.devlog_api.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlugProvider {
    private final PostRepository postRepository;

    /**
     * 제목을 기반으로 전역 유일 슬러그를 생성합니다.
     * 현재 정책: 전역 유일 (author 무관)
     */
    public String generateUniqueSlug(MemberEntity author, String title) {
        // SlugUtil이 있다면 그걸 사용, 없다면 간단한 slugify 로직 직접 구현
        String base = SlugUtil.slugify(title); // 예: 소문자, 비영문 -> -, 연속 - 압축, 앞뒤 - 트림
        if (base == null || base.isBlank()) base = "post";

        String candidate = base;
        int i = 0;
        while (postRepository.existsBySlug(candidate)) {
            i++;
            candidate = base + "-" + i;
        }
        return candidate;
    }
}
