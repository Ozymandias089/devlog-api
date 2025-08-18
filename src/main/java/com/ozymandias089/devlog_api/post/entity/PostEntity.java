package com.ozymandias089.devlog_api.post.entity;

import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "post",
        indexes = {
                @Index(name = "idx_post_author_created_at", columnList = "author_id, createdAt"),
                @Index(name = "idx_post_author_view_count", columnList = "author_id, viewCount")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Getter
    @Column(nullable = false, length = 160)
    private String slug;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private MemberEntity author;

    @Getter
    @Column(nullable = false, length = 200)
    private String title;

    @Getter
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String content;

    @Getter
    @Column(nullable = false)
    private Long viewCount = 0L;

    @Getter
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Builder
    public PostEntity(String slug, MemberEntity author, String title, String content, Long viewCount) {
        this.slug = slug;
        this.author = author;
        this.title = title;
        this.viewCount = viewCount;
        this.content = content;
    }

    public void increaseViewCount() { this.viewCount++; }
}
