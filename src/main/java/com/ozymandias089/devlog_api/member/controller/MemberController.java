package com.ozymandias089.devlog_api.member.controller;

import com.ozymandias089.devlog_api.member.dto.SignupRequestDTO;
import com.ozymandias089.devlog_api.member.dto.SignupResponseDTO;
import com.ozymandias089.devlog_api.member.dto.UserResponseDTO;
import com.ozymandias089.devlog_api.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping(value = "/signup", produces = "application/json")
    public ResponseEntity<SignupResponseDTO> signUp(@RequestBody @Valid SignupRequestDTO signupRequestDTO) {
        SignupResponseDTO responseDTO = memberService.signUp(signupRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }
}
// todo: 로그인 API, 로그아웃 API, 탈퇴 API, 비밀번호 찾기, 비밀번호 변경, 이메일 인증