package com.ozymandias089.devlog_api.member;

import com.ozymandias089.devlog_api.member.dto.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.dto.UserResponseDTO;
import com.ozymandias089.devlog_api.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MemberMapper {
    public Member toMemberEntity(SignupRequestDTO dto, String encodedPassword, String alias) {
        return Member.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .uuid(UUID.randomUUID())
                .username(alias)
                .build();
    }

    public UserResponseDTO toMemberResponseDTO(Member member, String username) {
        return UserResponseDTO.builder()
                .uuid(member.getUuid())
                .email(member.getEmail())
                .username(username)
                .build();
    }

    public SignupResponseDTO toSignupResponseDTO(UUID uuid, String email, String username, String accessToken, String refreshToken) {
        return SignupResponseDTO.builder()
                .uuid(uuid)
                .email(email)
                .username(username)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
