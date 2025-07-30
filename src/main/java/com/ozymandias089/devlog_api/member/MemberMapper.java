package com.ozymandias089.devlog_api.member;

import com.ozymandias089.devlog_api.member.dto.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.UserResponseDTO;
import com.ozymandias089.devlog_api.member.entity.Member;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MemberMapper {
    public Member toEntity(SignupRequestDTO dto, String encodedPassword, String alias) {
        return Member.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .uuid(UUID.randomUUID())
                .username(alias)
                .build();
    }

    public UserResponseDTO toResponse(Member member, String username) {
        return UserResponseDTO.builder()
                .uuid(member.getUuid())
                .email(member.getEmail())
                .username(username)
                .build();
    }
}
