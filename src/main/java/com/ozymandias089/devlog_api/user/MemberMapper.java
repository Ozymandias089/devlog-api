package com.ozymandias089.devlog_api.user;

import com.ozymandias089.devlog_api.user.dto.SignupRequestDTO;
import com.ozymandias089.devlog_api.user.dto.UserResponseDTO;
import com.ozymandias089.devlog_api.user.entity.Member;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MemberMapper {
    public Member toEntity(SignupRequestDTO dto) {
        return Member.builder()
                .email(dto.getEmail())
                .password(dto.getPassword()) // 암호화 예정
                .uuid(UUID.randomUUID())
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
