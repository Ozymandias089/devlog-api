package com.ozymandias089.devlog_api.post.service;

import com.ozymandias089.devlog_api.global.exception.InvalidCredentialsException;
import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import com.ozymandias089.devlog_api.member.repository.MemberRepository;
import com.ozymandias089.devlog_api.post.dto.request.CreatePostRequestDTO;
import com.ozymandias089.devlog_api.post.entity.PostEntity;
import com.ozymandias089.devlog_api.post.provider.PostMapper;
import com.ozymandias089.devlog_api.post.provider.SlugProvider;
import com.ozymandias089.devlog_api.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final SlugProvider slugProvider;

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
}
