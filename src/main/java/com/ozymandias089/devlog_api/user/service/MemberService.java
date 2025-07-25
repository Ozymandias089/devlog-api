package com.ozymandias089.devlog_api.user.service;

import com.ozymandias089.devlog_api.global.exception.DuplicateEmailExcpetion;
import com.ozymandias089.devlog_api.user.MemberMapper;
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
    private final MemberMapper mapper;

    @Transactional
    public UserResponseDTO signUp(SignupRequestDTO requestDTO) {
        if(repository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new DuplicateEmailExcpetion(requestDTO.getEmail());
        }
        Member member = mapper.toEntity(requestDTO);
        Member saved = repository.save(member);
        return mapper.toResponse(saved, generateUsername());
    }

    private String generateUsername() {
        int random = (int) (Math.random() * 1_000_000);// todo: include validation logic
        return String.format("User-%06d", random);
    }
}
