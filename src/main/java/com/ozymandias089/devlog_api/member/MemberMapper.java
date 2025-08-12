package com.ozymandias089.devlog_api.member;

import com.ozymandias089.devlog_api.global.enums.Role;
import com.ozymandias089.devlog_api.member.dto.request.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.response.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.UserResponseDTO;
import com.ozymandias089.devlog_api.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MemberMapper {
    public Member toMemberEntity(SignupRequestDTO dto, String encodedPassword, String alias, Role role) {
        return Member.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .uuid(UUID.randomUUID())
                .username(alias)
                .role(role)
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
