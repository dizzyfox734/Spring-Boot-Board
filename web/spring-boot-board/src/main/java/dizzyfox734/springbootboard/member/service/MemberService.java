package dizzyfox734.springbootboard.member.service;

import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.exception.InvalidRequestException;
import dizzyfox734.springbootboard.mail.domain.MailProperties;
import dizzyfox734.springbootboard.mail.exception.ExpiredMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.exception.InvalidMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.service.MailCertificationService;
import dizzyfox734.springbootboard.mail.service.MailRateLimitService;
import dizzyfox734.springbootboard.mail.service.MailService;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.exception.*;
import dizzyfox734.springbootboard.member.repository.AuthorityRepository;
import dizzyfox734.springbootboard.member.repository.MemberRepository;
import dizzyfox734.springbootboard.member.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MemberService {

    private static final String PASSWORD_RESET_TOKEN_INVALID_MESSAGE =
            "유효하지 않거나 만료된 비밀번호 재설정 링크입니다.";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final MailCertificationService mailCertificationService;
    private final MailRateLimitService mailRateLimitService;
    private final AuthorityRepository authorityRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailProperties mailProperties;

    /**
     * 회원 생성
     *
     * @param username 회원 아이디
     * @param password 비밀번호
     * @param name 이름
     * @param email 이메일
     * @param emailConfirm 이메일 인증코드
     * @return 생성된 회원의 id
     */
    @Transactional
    public Long create(String username, String password, String name, String email, String emailConfirm) {
        validateSignup(username, email, emailConfirm);

        Authority authority = authorityRepository.findById("ROLE_USER")
                .orElseThrow(() -> new AuthorityNotFoundException("ROLE_USER 권한이 존재하지 않습니다."));

        Member member = Member.create(
                username,
                passwordEncoder.encode(password),
                name,
                email,
                Collections.singleton(authority)
        );

        return memberRepository.save(member).getId();
    }

    /**
     * 회원 정보 수정 (지금은 비번만 바꿀 수 있음)
     *
     * @param member 수정할 회원 객체
     * @param password 새로운 비밀번호
     * @return 수정된 회원의 id
     */
    @Transactional
    public Long modify(Member member, String password) {
        member.changeEncodedPassword(passwordEncoder.encode(password));
        memberRepository.save(member);
        return member.getId();
    }

    /**
     * 회원 정보 가져오기
     *
     * @param username 회원의 아이디
     * @return 회원 객체
     * @throws DataNotFoundException 회원이 존재하지 않을 경우 예외를 던짐
     */
    @Transactional(readOnly = true)
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

    @Transactional
    public void createPasswordResetTokenAndSendEmail(String name, String email, String username, String clientIp) {
        mailRateLimitService.validatePasswordResetMailIpLimit(clientIp);
        Member member = findMemberForPasswordReset(name, email, username);
        mailRateLimitService.validatePasswordResetMailCooldown(member.getEmail());
        mailRateLimitService.markPasswordResetMailCooldown(member.getEmail());
        String previousToken = passwordResetTokenRepository.getTokenByUsername(member.getUsername());
        Duration previousExpiration = previousToken == null
                ? null
                : passwordResetTokenRepository.getExpirationByUsername(member.getUsername());
        String resetToken = generatePasswordResetToken();
        Duration expiration = Duration.ofSeconds(mailProperties.getPasswordResetExpirationSeconds());

        try {
            passwordResetTokenRepository.save(member.getUsername(), resetToken, expiration);
            mailService.sendPasswordResetEmail(member.getEmail(), resetToken);
        } catch (RuntimeException e) {
            mailRateLimitService.clearPasswordResetMailCooldown(member.getEmail());
            restorePreviousPasswordResetToken(member.getUsername(), resetToken, previousToken, previousExpiration);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public String getUsernameByPasswordResetToken(String token) {
        validatePasswordResetToken(token);

        String username = passwordResetTokenRepository.getUsernameByToken(token);
        if (username == null || username.isBlank()) {
            throw new InvalidRequestException(PASSWORD_RESET_TOKEN_INVALID_MESSAGE);
        }

        return username;
    }

    @Transactional
    public Long resetPasswordWithToken(String token, String newPassword) {
        String username = getUsernameByPasswordResetToken(token);
        Member member = getMember(username);

        member.changeEncodedPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
        passwordResetTokenRepository.removeByToken(token);

        return member.getId();
    }

    /**
     * 회원가입 검증
     *
     * @param username 회원 아이디
     * @param email 이메일
     * @param emailConfirm 이메일 인증코드
     */
    private void validateSignup(String username, String email, String emailConfirm) {
        validateUsernameNotDuplicated(username);
        validateEmailNotDuplicated(email);
        validateEmailVerified(email, emailConfirm);
    }

    private Member findMemberForPasswordReset(String name, String email, String username) {
        return memberRepository.findByNameAndEmailAndUsername(name, email, username)
                .orElseThrow(() -> new DataNotFoundException("No user found with the provided name and email"));
    }

    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private void restorePreviousPasswordResetToken(String username,
                                                   String newToken,
                                                   String previousToken,
                                                   Duration previousExpiration) {
        passwordResetTokenRepository.removeByToken(newToken);
        if (previousToken != null && previousExpiration != null) {
            passwordResetTokenRepository.save(username, previousToken, previousExpiration);
        }
    }

    private void validatePasswordResetToken(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidRequestException(PASSWORD_RESET_TOKEN_INVALID_MESSAGE);
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
