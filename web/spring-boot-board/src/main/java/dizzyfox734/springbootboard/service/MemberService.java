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
import java.util.Random;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    /**
     * 회원 생성
     *
     * @param signupDto 회원 가입에 필요한 정보를 담고 있는 DTO
     * @return 생성된 회원 객체
     */
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

    /**
     * 회원 정보 수정
     *
     * @param member 수정할 회원 객체
     * @param password 새로운 비밀번호
     * @return 수정된 회원 객체
     */
    @Transactional
    public Member modify(Member member, String password) {
        member.setPassword(passwordEncoder.encode(password));

        this.memberRepository.save(member);

        return member;
    }

    /**
     * 아이디 중복 검사
     *
     * @param username 검증할 아이디
     * @return 아이디가 중복되면 true, 아니면 false
     */
    public boolean validateDuplicateMember(String username) {
        return memberRepository.findOneWithAuthoritiesByUsername(username).isPresent();
    }

    /**
     * 이메일 중복 검사
     *
     * @param email 검증할 이메일
     * @return 이메일이 중복되면 true, 아니면 false
     */
    public boolean validateDuplicateEmail(String email) {
        return memberRepository.findOneWithAuthoritiesByEmail(email).isPresent();
    }

    /**
     * 회원 정보 가져오기
     *
     * @param username 회원의 아이디
     * @return 회원 객체
     * @throws DataNotFoundException 회원이 존재하지 않을 경우 예외를 던짐
     */
    public Member getMember(String username) {
        Optional<Member> member = this.memberRepository.findOneWithAuthoritiesByUsername(username);
        if (member.isPresent()) {
            return member.get();
        } else {
            throw new DataNotFoundException("user not found");
        }
    }

    /**
     * 이름과 이메일을 통해 아이디를 찾기
     *
     * @param name 이름
     * @param email 이메일
     * @return 아이디
     * @throws DataNotFoundException 아이디를 찾을 수 없으면 예외를 던짐
     */
    @Transactional
    public String findUsername(String name, String email) {
        Optional<Member> member = this.memberRepository.findByNameAndEmail(name, email);
        if (member.isPresent()) {
            return member.get().getUsername();
        } else {
            throw new DataNotFoundException("No user found with the provided name and email");
        }
    }

    /**
     * 이름, 이메일, 아이디로 회원 정보를 조회하여 존재 여부 확인
     *
     * @param name 이름
     * @param email 이메일
     * @param username 아이디
     * @return 회원이 존재하면 true, 아니면 false
     */
    @Transactional
    public boolean existEmail(String name, String email, String username) {
        Optional<Member> member = this.memberRepository.findByNameAndEmailAndUsername(name, email, username);
        return member.isPresent();
    }

    /**
     * 임시 비밀번호를 생성하여 해당 이메일로 전송
     *
     * @return 임시 비밀번호
     */
    @Transactional
    public String resetPasswordAndSendEmail(String name, String email, String username) {
        String temporaryPwd = generateTemporaryPassword();

        Optional<Member> memberOptional = memberRepository.findByNameAndEmailAndUsername(name, email, username);
        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            member.setPassword(passwordEncoder.encode(temporaryPwd));
            memberRepository.save(member);

            try {
                mailService.sendTemporaryPasswordEmail(email, temporaryPwd);
            } catch (Exception e) {
                throw new RuntimeException("이메일 전송 실패", e);
            }

            return temporaryPwd;
        } else {
            throw new DataNotFoundException("No user found with the provided name and email");
        }
    }

    /**
     * 임시 비밀번호를 생성
     *
     * @return 생성된 임시 비밀번호
     */
    private String generateTemporaryPassword() {
        // 8자리 랜덤 임시 비밀번호 생성 (알파벳 + 숫자)
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append((char) (random.nextInt(26) + 'a')); // a~z
        }
        return password.toString();
    }

}