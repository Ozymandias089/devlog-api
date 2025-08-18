package com.ozymandias089.devlog_api.post.provider;

import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import com.ozymandias089.devlog_api.post.entity.PostEntity;
import org.springframework.stereotype.Component;

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
}
