package com.ozymandias089.devlog_api.member.controller;

import com.ozymandias089.devlog_api.member.dto.request.*;
import com.ozymandias089.devlog_api.member.dto.response.LoginResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.PasswordResetResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.PasswordValidationResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;

/**
 * REST Controller for managing member-related operations.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *     <li>회원가입</li>
 *     <li>비밀번호 유효성 검사</li>
 *     <li>이메일 중복 확인</li>
 *     <li>로그인 / 로그아웃</li>
 *     <li>회원 탈퇴</li>
 *     <li>비밀번호 재설정 요청, 검증, 확정</li>
 * </ul>
 * All endpoints except signup, email check, password validation, and login require JWT authentication.
 * </p>
 *
 * @author Younghoon Choi
 * @since 1.0
 */
@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "User management APIs")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    /**
     * Registers a new member account.
     *
     * @param signupRequestDTO the DTO containing email, password, and other registration details
     * @return the registered member's UUID, email, username, and issued access/refresh tokens
     */
    @PostMapping(value = "/signup", produces = "application/json")
    @Operation(summary = "회원가입", description = "회원 정보를 입력받아 새로운 사용자를 등록하고 등록된 회원정보를 반환합니다.")
    public ResponseEntity<SignupResponseDTO> signUp(@RequestBody @Valid SignupRequestDTO signupRequestDTO) {
        SignupResponseDTO responseDTO = memberService.signUp(signupRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Validates a password against server-side complexity rules.
     *
     * @param requestDTO the DTO containing the password to validate
     * @return a DTO indicating validity and any rule violations
     */
    @PostMapping(value = "/password/validate", produces = "application/json")
    @Operation(summary = "Validate Password", description = "비밀번호 유효성을 검사합니다.")
    public ResponseEntity<PasswordValidationResponseDTO> validatePassword(@RequestBody @Valid PasswordValidationRequestDTO requestDTO) {
        PasswordValidationResponseDTO responseDTO = memberService.validatePassword(requestDTO.getPassword());
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Checks if the given email is available for registration.
     *
     * @param email the email to check
     * @return {@code true} if the email is valid and not yet registered, {@code false} otherwise
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check Email Duplication", description = "Returns true if the email is valid and available for registration.")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean result = memberService.isEmailValidAndAvailable(email);
        return ResponseEntity.ok(result);
    }

    /**
     * Authenticates a user and issues new access and refresh tokens.
     *
     * @param requestDTO the DTO containing login credentials
     * @return a DTO containing access/refresh tokens
     */
    @PostMapping(value = "/login", produces = "application/json")
    @Operation(summary = "Login", description = "Logs in to service. Produces Refresh and Access Tokens")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO requestDTO) {
        LoginResponseDTO responseDTO = memberService.login(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Logs out the currently authenticated user.
     * <p>
     * This will:
     * <ul>
     *     <li>Delete the refresh token from Redis</li>
     *     <li>Blacklist the current access token until its natural expiration</li>
     * </ul>
     *
     * @param userPrincipal       the authenticated user's principal (UUID)
     * @param authorizationHeader the "Authorization" HTTP header containing the bearer access token
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "로그아웃하고 Refresh Token을 무효화합니다.")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestHeader("Authorization") String authorizationHeader) {
        memberService.logout(userPrincipal.getName(), authorizationHeader);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes the currently authenticated user's account after verifying their password.
     *
     * @param userPrincipal the authenticated user's principal (UUID)
     * @param requestDTO    the DTO containing the password for confirmation
     */
    @DeleteMapping("/unregister")
    @Operation(summary = "Delete Account", description = "회원 탈퇴를 진행합니다.")
    public ResponseEntity<Void> unregister(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody @Valid PasswordCheckRequestDTO requestDTO) {
        memberService.deleteMember(userPrincipal.getName(), requestDTO.getPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the nickname (username) of the currently authenticated member.
     * <p>
     * The member is identified through the {@link UserPrincipal} extracted from the JWT token.
     * The new username is provided via a {@link UsernameUpdateRequestDTO} request body.
     * </p>
     *
     * @param userPrincipal     The authenticated user's principal, containing their UUID.
     * @param updateRequestDTO  DTO containing the new username to update.
     * @return {@link ResponseEntity} with status 200 OK if the update is successful.
     */
    @PatchMapping("/update-username")
    @Operation(summary = "Update Username", description = "닉네임을 업데이트합니다.")
    public ResponseEntity<Void> updateUsername(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody @Valid UsernameUpdateRequestDTO updateRequestDTO) {
        memberService.updateUsername(userPrincipal.getName(), updateRequestDTO.getNewUsername());
        return ResponseEntity.ok().build();
    }

    /***********************************************************
     *                Password Reset Pipeline                  *
     ***********************************************************/

    /**
     * Requests a password reset link for the given email.
     *
     * @param requestDTO the DTO containing the registered email
     */
    @PostMapping(value = "/password-reset/request", produces = "application/json")
    @Operation(summary = "Request Password Reset", description = "회원 이메일을 입력받아 비밀번호 재설정 링크를 이메일로 전송합니다.")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDTO requestDTO) {
        memberService.requestPasswordReset(requestDTO.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/password-reset/issue", produces = "application/json")
    @Operation( summary = "Issue Password Reset Token (Authenticated)", description = "로그인 상태에서 현재 비밀번호를 재확인한 뒤, 짧은 만료의 1회성 비밀번호 재설정 토큰을 발급합니다." )
    public ResponseEntity<PasswordResetResponseDTO> issueResetToken(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody @Valid PasswordCheckRequestDTO requestDTO) {
        PasswordResetResponseDTO passwordResetResponseDTO = memberService.issueResetToken(userPrincipal.getName(), requestDTO.getPassword());
        return ResponseEntity.ok(passwordResetResponseDTO);
    }

    /**
     * Verifies whether a password reset token is still valid.
     *
     * @param resetToken the password reset token to verify
     * @return {@code true} if the token is valid and not expired, {@code false} otherwise
     */
    @GetMapping(value = "/password-reset/verify", produces = "application/json")
    @Operation(summary = "Verify Password Reset Token", description = "비밀번호 재설정 토큰의 유효 여부를 검사합니다.")
    public ResponseEntity<Boolean> verifyResetToken(@RequestParam String resetToken) {
        boolean valid = memberService.isPasswordResetTokenValid(resetToken);
        return ResponseEntity.ok(valid);
    }

    /**
     * Resets a user's password using a valid reset token.
     *
     * @param requestDTO the DTO containing the reset token and new password
     */
    @PostMapping(value = "/password-reset/confirm", produces = "application/json")
    @Operation(summary = "Confirm Password Reset", description = "유효한 토큰과 새로운 비밀번호를 입력받아 비밀번호를 재설정합니다.")
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody @Valid PasswordResetConfirmRequestDTO requestDTO) {
        memberService.resetPassword(requestDTO);
        return ResponseEntity.ok().build();
    }
}