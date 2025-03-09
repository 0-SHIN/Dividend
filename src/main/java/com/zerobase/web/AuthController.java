package com.zerobase.web;

import com.zerobase.model.Auth;
import com.zerobase.persist.entity.MemberEntity;
import com.zerobase.security.TokenProvider;
import com.zerobase.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final MemberService memberService;

    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request) {
        MemberEntity result = this.memberService.register(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request) {
        MemberEntity member = this.memberService.authenticate(request);
        String token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());
        log.info("user login -> {}", request.getUsername());

        return ResponseEntity.ok(token);
    }
}
