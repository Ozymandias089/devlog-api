package com.ozymandias089.devlog_api.member.controller;

import com.ozymandias089.devlog_api.member.dto.request.*;
import com.ozymandias089.devlog_api.member.dto.response.LoginResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.PasswordValidationResponseDTO;
import com.ozymandias089.devlog_api.member.dto.response.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "User management APIs")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping(value = "/signup", produces = "application/json")
    @Operation(summary = "회원가입", description = "회원 정보를 입력받아 새로운 사용자를 등록하고 등록된 회원정보를 반환합니다.")
    public ResponseEntity<SignupResponseDTO> signUp(@RequestBody @Valid SignupRequestDTO signupRequestDTO) {
        SignupResponseDTO responseDTO = memberService.signUp(signupRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping(value = "/password/validate", produces = "application/json")
    @Operation(summary = "Validate Password", description = "비밀번호 유효성을 검사합니다.")
    public ResponseEntity<PasswordValidationResponseDTO> vaidatePassword(@RequestBody @Valid PasswordValidationRequestDTO requestDTO) {
        PasswordValidationResponseDTO responseDTO = memberService.validatePassword(requestDTO.getPassword());
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check Email Duplication", description = "Returns true if the email is valid and available for registration.")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean result = memberService.isEmailValidAndAvailable(email);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/login", produces = "application/json")
    @Operation(summary = "Login", description = "Logs in to service. Produces Refresh and Access Tokens")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO requestDTO) {
        LoginResponseDTO responseDTO = memberService.login(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping(value = "/password-reset/request", produces = "application/json")
    @Operation(summary = "Request Password Reset", description = "회원 이메일을 입력받아 비밀번호 재설정 링크를 이메일로 전송합니다.")
    public ResponseEntity<Void> requestPasswordReset(@RequestBody @Valid PasswordResetRequestDTO requestDTO) {
        memberService.requestPasswordReset(requestDTO.getEmail());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/password-reset/verify", produces = "application/json")
    @Operation(summary = "Verify Password Reset Token", description = "비밀번호 재설정 토큰의 유효 여부를 검사합니다.")
    public ResponseEntity<Boolean> verifyResetToken(@RequestParam String resetToken) {
        boolean valid = memberService.isPasswordResetTokenValid(resetToken);
        return ResponseEntity.ok(valid);
    }

    @PostMapping(value = "/password-reset/confirm", produces = "application/json")
    @Operation(summary = "Confirm Password Reset", description = "유효한 토큰과 새로운 비밀번호를 입력받아 비밀번호를 재설정합니다.")
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody @Valid PasswordResetConfirmRequestDTO requestDTO) {
        memberService.resetPassword(requestDTO);
        return ResponseEntity.ok().build();
    }
}
// todo:로그아웃 API, 탈퇴 API, 비밀번호 찾기, 비밀번호 변경