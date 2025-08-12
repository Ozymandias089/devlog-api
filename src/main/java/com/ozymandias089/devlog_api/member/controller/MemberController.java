package com.ozymandias089.devlog_api.member.controller;

import com.ozymandias089.devlog_api.member.dto.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.dto.UserResponseDTO;
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

    @GetMapping("/check-email")
    @Operation(summary = "Check Email Duplication", description = "Returns True if email is already taken")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        boolean result = memberService.isEmailValidAndAvailable(email);
        return ResponseEntity.ok(result);
    }
}
// todo: 로그인 API, 로그아웃 API, 탈퇴 API, 비밀번호 찾기, 비밀번호 변경, 이메일 인증