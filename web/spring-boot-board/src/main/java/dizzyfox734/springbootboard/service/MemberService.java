package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.domain.member.Authority;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.domain.member.MemberRepository;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Member create(SignupDto signupDto) {

        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();

        Member member = Member.builder()
                .username(signupDto.getUsername())
                .password(passwordEncoder.encode(signupDto.getPassword1()))
                .name(signupDto.getName())
                .email(signupDto.getEmail())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        this.memberRepository.save(member);

        return member;
    }

    @Transactional
    public Member modify(Member member, String password) {
        member.setPassword(passwordEncoder.encode(password));

        this.memberRepository.save(member);

        return member;
    }

    public boolean validateDuplicateMember(String username) {
        return memberRepository.findOneWithAuthoritiesByUsername(username).isPresent();
    }

    public boolean validateDuplicateEmail(String email) {
        return memberRepository.findOneWithAuthoritiesByEmail(email).isPresent();
    }

    public Member getMember(String username) {
        Optional<Member> member = this.memberRepository.findOneWithAuthoritiesByUsername(username);
        if (member.isPresent()) {
            return member.get();
        } else {
            throw new DataNotFoundException("user not found");
        }
    }
}