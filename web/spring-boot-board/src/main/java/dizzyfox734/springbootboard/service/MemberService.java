package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.domain.mail.MailCertificationService;
import dizzyfox734.springbootboard.domain.member.Authority;
import dizzyfox734.springbootboard.domain.member.AuthorityRepository;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.domain.member.MemberRepository;
import dizzyfox734.springbootboard.exception.AuthorityNotFoundException;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
import dizzyfox734.springbootboard.exception.DuplicateEmailException;
import dizzyfox734.springbootboard.exception.DuplicateUsernameException;
import dizzyfox734.springbootboard.exception.EmailVerificationException;
import dizzyfox734.springbootboard.exception.ExpiredMailCertificationCodeException;
import dizzyfox734.springbootboard.exception.InvalidMailCertificationCodeException;
import dizzyfox734.springbootboard.exception.MailMessageBuildException;
import dizzyfox734.springbootboard.exception.MailSendException;
import dizzyfox734.springbootboard.exception.PasswordMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class MemberService {

    private static final int TEMPORARY_PASSWORD_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final MailCertificationService mailCertificationService;
    private final AuthorityRepository authorityRepository;

    /**
     * 회원 생성
     *
     * @param signupDto 회원 가입에 필요한 정보를 담고 있는 DTO
     * @return 생성된 회원 객체
     */
    @Transactional
    public Member create(SignupDto signupDto) {
        validateSignup(signupDto);

        Authority authority = authorityRepository.findById("ROLE_USER")
                .orElseThrow(() -> new AuthorityNotFoundException("ROLE_USER 권한이 존재하지 않습니다."));

        Member member = Member.builder()
                .username(signupDto.getUsername())
                .password(passwordEncoder.encode(signupDto.getPassword1()))
                .name(signupDto.getName())
                .email(signupDto.getEmail())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        return memberRepository.save(member);
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
        return memberRepository.save(member);
    }

    /**
     * 회원 정보 가져오기
     *
     * @param username 회원의 아이디
     * @return 회원 객체
     * @throws DataNotFoundException 회원이 존재하지 않을 경우 예외를 던짐
     */
    public Member getMember(String username) {
        return memberRepository.findOneWithAuthoritiesByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("user not found"));
    }

    /**
     * 이름과 이메일을 통해 아이디를 찾기
     *
     * @param name 이름
     * @param email 이메일
     * @return 아이디
     * @throws DataNotFoundException 아이디를 찾을 수 없으면 예외를 던짐
     */
    @Transactional(readOnly = true)
    public String findUsername(String name, String email) {
        return memberRepository.findByNameAndEmail(name, email)
                .map(Member::getUsername)
                .orElseThrow(() -> new DataNotFoundException("No user found with the provided name and email"));
    }

    /**
     * 이름, 이메일, 아이디로 회원 정보를 조회하여 존재 여부 확인
     *
     * @param name 이름
     * @param email 이메일
     * @param username 아이디
     * @return 회원이 존재하면 true, 아니면 false
     */
    @Transactional(readOnly = true)
    public boolean existEmail(String name, String email, String username) {
        return memberRepository.findByNameAndEmailAndUsername(name, email, username).isPresent();
    }

    /**
     * 임시 비밀번호를 생성하여 해당 이메일로 전송
     *
     * @return 임시 비밀번호
     */
    @Transactional
    public String resetPasswordAndSendEmail(String name, String email, String username) {
        Member member = memberRepository.findByNameAndEmailAndUsername(name, email, username)
                .orElseThrow(() -> new DataNotFoundException("No user found with the provided name and email"));

        String temporaryPwd = generateTemporaryPassword();
        member.setPassword(passwordEncoder.encode(temporaryPwd));
        memberRepository.save(member);

        try {
            mailService.sendTemporaryPasswordEmail(email, temporaryPwd);
        } catch (MailSendException | MailMessageBuildException e) {
            throw e;
        }

        return temporaryPwd;
    }

    /**
     * 임시 비밀번호를 생성
     *
     * @return 생성된 임시 비밀번호
     */
    private String generateTemporaryPassword() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < TEMPORARY_PASSWORD_LENGTH; i++) {
            password.append(chars.charAt(RANDOM.nextInt(chars.length())));
        }

        return password.toString();
    }

    /**
     * 회원가입 검증
     *
     * @param signupDto 회원 가입에 필요한 정보를 담은 DTO
     */
    private void validateSignup(SignupDto signupDto) {
        validatePasswordMatch(signupDto);
        validateUsernameNotDuplicated(signupDto.getUsername());
        validateEmailNotDuplicated(signupDto.getEmail());
        validateEmailVerified(signupDto.getEmail(), signupDto.getEmailConfirm());
    }

    private void validatePasswordMatch(SignupDto signupDto) {
        if (!signupDto.getPassword1().equals(signupDto.getPassword2())) {
            throw new PasswordMismatchException("패스워드가 일치하지 않습니다.");
        }
    }

    private void validateUsernameNotDuplicated(String username) {
        if (memberRepository.findOneWithAuthoritiesByUsername(username).isPresent()) {
            throw new DuplicateUsernameException("이미 등록된 아이디입니다.");
        }
    }

    private void validateEmailNotDuplicated(String email) {
        if (memberRepository.findOneWithAuthoritiesByEmail(email).isPresent()) {
            throw new DuplicateEmailException("이미 등록된 이메일입니다.");
        }
    }

    private void validateEmailVerified(String email, String emailConfirm) {
        try {
            mailCertificationService.verifyEmailCertificationCode(email, emailConfirm);
        } catch (ExpiredMailCertificationCodeException | InvalidMailCertificationCodeException e) {
            throw new EmailVerificationException(e.getMessage());
        }
    }
}
