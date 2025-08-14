package com.ozymandias089.devlog_api.member.provider;

import com.ozymandias089.devlog_api.global.enums.Role;
import com.ozymandias089.devlog_api.member.PasswordValidationResult;
import com.ozymandias089.devlog_api.member.dto.request.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.response.*;
import com.ozymandias089.devlog_api.member.entity.MemberEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MemberMapper {
    /**
     * SignupRequestDTO와 추가 정보를 받아 Member 엔티티로 변환합니다.
     *
     * @param dto 회원가입 요청 DTO (이메일 등 사용자 입력 데이터)
     * @param encodedPassword 암호화된 비밀번호
     * @param alias 생성된 별명 또는 사용자명
     * @param role 회원 역할(Role enum)
     * @return 생성된 Member 엔티티 객체
     */
    public MemberEntity toMemberEntity(SignupRequestDTO dto, String encodedPassword, String alias, Role role) {
        return MemberEntity.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .uuid(UUID.randomUUID())
                .username(alias)
                .role(role)
                .build();
    }

    /**
     * Member 엔티티와 사용자명을 받아 UserResponseDTO로 변환합니다.
     *
     * @param member 변환 대상 Member 엔티티
     * @param username 사용자명
     * @return 사용자 응답 DTO
     */
    public UserResponseDTO toMemberResponseDTO(MemberEntity member, String username) {
        return UserResponseDTO.builder()
                .uuid(member.getUuid())
                .email(member.getEmail())
                .username(username)
                .build();
    }

    /**
     * 회원가입 성공 시 반환할 SignupResponseDTO 객체를 생성합니다.
     *
     * @param uuid 회원 고유 식별자
     * @param email 회원 이메일
     * @param username 회원명
     * @param accessToken 발급된 JWT Access Token
     * @param refreshToken 발급된 JWT Refresh Token
     * @return 회원가입 응답 DTO
     */
    public SignupResponseDTO toSignupResponseDTO(UUID uuid, String email, String username, String accessToken, String refreshToken) {
        return SignupResponseDTO.builder()
                .uuid(uuid)
                .email(email)
                .username(username)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 주어진 비밀번호 유효성 검사 결과를 {@link PasswordValidationResponseDTO}로 변환합니다.
     *
     * <p>유효성 여부와 발생한 에러 메시지 목록을 DTO에 담아 반환합니다.</p>
     *
     * @param validity 비밀번호 유효성 검사 결과 객체
     * @return 유효성 여부와 에러 메시지가 포함된 {@link PasswordValidationResponseDTO}
     */
    public PasswordValidationResponseDTO toPasswordValidationResponseDTO(PasswordValidationResult validity) {
        return PasswordValidationResponseDTO.builder()
                .isValid(validity.validity())
                .errors(validity.errors())
                .build();
    }

    /**
     * 발급된 액세스 토큰과 리프레시 토큰을 {@link LoginResponseDTO}로 변환합니다.
     *
     * <p>로그인 성공 시 클라이언트에 반환할 인증 토큰 정보를 DTO 형태로 구성합니다.</p>
     *
     * @param accessToken  JWT 액세스 토큰
     * @param refreshToken JWT 리프레시 토큰
     * @return 액세스 토큰과 리프레시 토큰이 포함된 {@link LoginResponseDTO}
     */
    public LoginResponseDTO toLoginResponseDTO(String accessToken, String refreshToken) {
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 발급된 비밀번호 재설정 토큰을 {@link PasswordResetResponseDTO}로 변환합니다.
     *
     * <p>비밀번호 재설정 절차에서 클라이언트에 전달할 토큰 정보를 DTO 형태로 구성합니다.</p>
     *
     * @param resetToken 비밀번호 재설정용 JWT 토큰
     * @return 재설정 토큰이 포함된 {@link PasswordResetResponseDTO}
     */
    public PasswordResetResponseDTO toPasswordResetResponseDTO(String resetToken) {
        return PasswordResetResponseDTO.builder()
                .resetToken(resetToken)
                .build();
    }
}
