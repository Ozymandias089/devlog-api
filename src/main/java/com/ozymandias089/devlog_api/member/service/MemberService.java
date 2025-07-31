package com.ozymandias089.devlog_api.member.service;

import com.ozymandias089.devlog_api.auth.jwt.JwtTokenProvider;
import com.ozymandias089.devlog_api.global.enums.Role;
import com.ozymandias089.devlog_api.global.exception.DuplicateEmailExcpetion;
import com.ozymandias089.devlog_api.member.MemberMapper;
import com.ozymandias089.devlog_api.member.dto.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.dto.UserResponseDTO;
import com.ozymandias089.devlog_api.member.entity.Member;
import com.ozymandias089.devlog_api.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository repository;
    private final MemberMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponseDTO signUp(SignupRequestDTO requestDTO) {
        if(repository.findByEmail(requestDTO.getEmail()).isPresent()) throw new DuplicateEmailExcpetion(requestDTO.getEmail());

        // Encode password
        String encodedPassword = hashPassword(requestDTO.getPassword());

        // Random username creation
        String username = generateUsername();

        Role defaultRole = Role.ROLE_USER;

        // Create / save Member Entity
        Member member = mapper.toMemberEntity(requestDTO, encodedPassword, username, defaultRole);
        Member saved = repository.save(member);

        // Create JWT AnR Tokens
        String accessToken = jwtTokenProvider.generateAccessToken(saved.getUuid().toString(), defaultRole);
        String refreshToken = jwtTokenProvider.generateRefreshToken(saved.getUuid().toString());

        return mapper.toSignupResponseDTO(saved.getUuid(), saved.getEmail(), saved.getUsername(), accessToken, refreshToken);
    }

    private String generateUsername() {
        int random = (int) (Math.random() * 1_000_000);// todo: include validation logic
        return String.format("User-%06d", random);
    }

    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

}
