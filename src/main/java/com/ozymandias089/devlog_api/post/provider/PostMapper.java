package com.ozymandias089.devlog_api.post.provider;

import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import com.ozymandias089.devlog_api.post.dto.PostSummaryDTO;
import com.ozymandias089.devlog_api.post.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostMapper {

    public static PostEntity toPostEntity(MemberEntity member, String title, String content, String slug) {
        return PostEntity.builder()
                .author(member)
                .title(title)
                .content(content)
                .slug(slug)
                .build();
    }

    public static List<PostSummaryDTO> toPostSummaryDTOs(Page<PostEntity> pageData) {
        return pageData.getContent().stream()
                .map(p -> new PostSummaryDTO(
                        p.getTitle(),
                        p.getSlug(),
                        p.getAuthor().getUuid(),
                        p.getAuthor().getUsername(),
                        p.getViewCount(),
                        p.getCreatedAt()
                ))
                .toList();
    }
}
