package com.ozymandias089.devlog_api.user.controller;

import com.ozymandias089.devlog_api.user.dto.SignupRequestDTO;
import com.ozymandias089.devlog_api.user.dto.UserResponseDTO;
import com.ozymandias089.devlog_api.user.service.MemberService;
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

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signUp(@RequestBody @Valid SignupRequestDTO signupRequestDTO) {
        UserResponseDTO responseDTO = memberService.signUp(signupRequestDTO);
        return ResponseEntity.ok(responseDTO);
    }
}
