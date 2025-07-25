package com.ozymandias089.devlog_api.user.service;

import com.ozymandias089.devlog_api.global.exception.DuplicateEmailExcpetion;
import com.ozymandias089.devlog_api.user.dto.SignupRequestDTO;
import com.ozymandias089.devlog_api.user.dto.UserResponseDTO;
import com.ozymandias089.devlog_api.user.entity.Member;
import com.ozymandias089.devlog_api.user.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;

    @Transactional
    public UserResponseDTO signUp(SignupRequestDTO requestDTO) {
        if(repository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new DuplicateEmailExcpetion(requestDTO.getEmail());
        }

        Member member = Member.builder()
                .email(requestDTO.getEmail())
                .password(requestDTO.getPassword()) // todo: Implement Password encryption
                .uuid(UUID.randomUUID())
                .build();

        Member saved = repository.save(member);

        return UserResponseDTO.builder()
                .uuid(saved.getUuid())
                .email(saved.getEmail())
                .username(generateUsername())
                .build();
    }

    private String generateUsername() {
        int random = (int) (Math.random() * 1_000_000);
        return String.format("User-%06d", random);
    }
}
