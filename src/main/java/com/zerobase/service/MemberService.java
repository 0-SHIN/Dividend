package com.zerobase.service;

import com.zerobase.exception.impl.AlreadyExistUserException;
import com.zerobase.exception.impl.NoUserException;
import com.zerobase.exception.impl.PasswordMismatchException;
import com.zerobase.model.Auth;
import com.zerobase.persist.MemberRepository;
import com.zerobase.persist.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new NoUserException());
    }

    public MemberEntity register(Auth.SignUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword()));
        MemberEntity result = this.memberRepository.save(member.toEntity());

        return result;
    }

    public MemberEntity authenticate(Auth.SignIn member) {
        MemberEntity user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(() -> new NoUserException());

        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new PasswordMismatchException();
        }
        return user;
    }

}

