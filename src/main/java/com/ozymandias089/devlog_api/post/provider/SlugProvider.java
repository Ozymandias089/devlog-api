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

    public String generateUniqueSlug(MemberEntity author, String title) {
        String base = SlugUtil.toSlug(title);
        String slug = base;

        // 1~5까지 숫자 suffix 시도
        for (int i = 2; i <= 5; i++) {
            if (!postRepository.existsByAuthorAndSlug(author, slug)) return slug;
            slug = base + "-" + i;
            if (slug.length() > 160) {
                slug = (base.substring(0, Math.max(1, 160 - ("-" + i).length()))) + "-" + i;
            }
        }
        // 여전히 충돌 → 짧은 토큰
        String tok = SlugUtil.shortToken();
        slug = base + "-" + tok;
        if (slug.length() > 160) {
            slug = base.substring(0, Math.max(1, 160 - ("-" + tok).length())) + "-" + tok;
        }
        return slug;
    }
}
