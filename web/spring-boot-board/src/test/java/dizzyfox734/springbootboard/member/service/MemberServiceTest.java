package dizzyfox734.springbootboard.member.service;

import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.exception.InvalidRequestException;
import dizzyfox734.springbootboard.mail.domain.MailProperties;
import dizzyfox734.springbootboard.mail.exception.ExpiredMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.exception.InvalidMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.exception.MailMessageBuildException;
import dizzyfox734.springbootboard.mail.exception.MailSendException;
import dizzyfox734.springbootboard.mail.service.MailCertificationService;
import dizzyfox734.springbootboard.mail.service.MailRateLimitService;
import dizzyfox734.springbootboard.mail.service.MailService;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.exception.AuthorityNotFoundException;
import dizzyfox734.springbootboard.member.exception.DuplicateEmailException;
import dizzyfox734.springbootboard.member.exception.DuplicateUsernameException;
import dizzyfox734.springbootboard.member.exception.EmailVerificationException;
import dizzyfox734.springbootboard.member.repository.AuthorityRepository;
import dizzyfox734.springbootboard.member.repository.MemberRepository;
import dizzyfox734.springbootboard.member.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @Mock
    private MailCertificationService mailCertificationService;

    @Mock
    private AuthorityRepository authorityRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private MailProperties mailProperties;

    @Mock
    private MailRateLimitService mailRateLimitService;

    @InjectMocks
    private MemberService memberService;

    private Long createMember() {
        return memberService.create(
                "testuser",
                "password123",
                "홍길동",
                "test@example.com",
                "123456"
        );
    }

    private Authority createAuthority() {
        return Authority.builder()
                .name("ROLE_USER")
                .build();
    }

    private Member createMember(String username, String encodedPassword, String name, String email) {
        return Member.create(
                username,
                encodedPassword,
                name,
                email,
                Set.of(createAuthority())
        );
    }

    private void setMemberId(Member member, Long id) {
        try {
            Field field = Member.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(member, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("create(): 유효한 회원가입 정보가 주어지면 회원을 생성하고 저장한 뒤 회원 ID를 반환한다")
    void shouldCreateMemberAndReturnMemberId_whenCreateRequestIsValid() {
        // given
        Authority roleUser = createAuthority();

        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(authorityRepository.findById("ROLE_USER"))
                .thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");

        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> {
                    Member savedMember = invocation.getArgument(0);
                    setMemberId(savedMember, 1L);
                    return savedMember;
                });

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        // when
        Long result = createMember();

        // then
        assertEquals(1L, result);

        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService).verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository).findById("ROLE_USER");
        verify(passwordEncoder).encode("password123");
        verify(memberRepository).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertEquals("testuser", capturedMember.getUsername());
        assertEquals("홍길동", capturedMember.getName());
        assertEquals("test@example.com", capturedMember.getEmail());
        assertEquals("encodedPassword", capturedMember.getPassword());
        assertNotEquals("password123", capturedMember.getPassword());
        assertNotNull(capturedMember.getAuthorities());
        assertEquals(1, capturedMember.getAuthorities().size());
        assertTrue(capturedMember.getAuthorities().contains(roleUser));
    }

    @Test
    @DisplayName("create(): username이 중복되면 DuplicateUsernameException이 발생한다")
    void shouldThrowDuplicateUsernameException_whenUsernameAlreadyExists() {
        // given
        Member existingMember = createMember(
                "testuser",
                "encodedPassword",
                "기존회원",
                "exist@example.com"
        );

        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.of(existingMember));

        // when
        DuplicateUsernameException exception = assertThrows(
                DuplicateUsernameException.class,
                () -> createMember()
        );

        // then
        assertEquals("이미 등록된 아이디입니다.", exception.getMessage());
        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository, never()).findOneWithAuthoritiesByEmail(anyString());
        verify(mailCertificationService, never()).verifyEmailCertificationCode(anyString(), anyString());
        verify(authorityRepository, never()).findById(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): email이 중복되면 DuplicateEmailException이 발생한다")
    void shouldThrowDuplicateEmailException_whenEmailAlreadyExists() {
        // given
        Member existingMember = createMember(
                "existingMember",
                "encodedPassword",
                "기존회원",
                "test@example.com"
        );

        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.of(existingMember));

        // when
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> createMember()
        );

        // then
        assertEquals("이미 등록된 이메일입니다.", exception.getMessage());
        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService, never()).verifyEmailCertificationCode(anyString(), anyString());
        verify(authorityRepository, never()).findById(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): 인증코드가 일치하지 않으면 EmailVerificationException이 발생한다")
    void shouldThrowEmailVerificationException_whenCertificationCodeIsInvalid() {
        // given
        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        doThrow(new InvalidMailCertificationCodeException())
                .when(mailCertificationService)
                .verifyEmailCertificationCode("test@example.com", "123456");

        // when
        EmailVerificationException exception = assertThrows(
                EmailVerificationException.class,
                () -> createMember()
        );

        // then
        assertEquals("인증코드가 올바르지 않습니다.", exception.getMessage());
        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService).verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository, never()).findById(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): 인증코드가 없거나 만료되면 EmailVerificationException이 발생한다")
    void shouldThrowEmailVerificationException_whenCertificationCodeIsExpired() {
        // given
        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        doThrow(new ExpiredMailCertificationCodeException())
                .when(mailCertificationService)
                .verifyEmailCertificationCode("test@example.com", "123456");

        // when
        EmailVerificationException exception = assertThrows(
                EmailVerificationException.class,
                () -> createMember()
        );

        // then
        assertEquals("인증코드가 없거나 만료되었습니다.", exception.getMessage());
        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService).verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository, never()).findById(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): 기본 권한 조회에 실패하면 AuthorityNotFoundException이 발생한다")
    void shouldThrowAuthorityNotFoundException_whenDefaultAuthorityIsMissing() {
        // given
        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(authorityRepository.findById("ROLE_USER"))
                .thenReturn(Optional.empty());

        // when
        AuthorityNotFoundException exception = assertThrows(
                AuthorityNotFoundException.class,
                () -> createMember()
        );

        // then
        assertEquals("ROLE_USER 권한이 존재하지 않습니다.", exception.getMessage());
        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService).verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository).findById("ROLE_USER");
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): 저장 중 repository에서 예외가 발생하면 예외가 전파된다")
    void shouldPropagateException_whenRepositorySaveFailsInCreate() {
        // given
        Authority roleUser = createAuthority();

        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(authorityRepository.findById("ROLE_USER"))
                .thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new RuntimeException("DB save failed"));

        // when
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> createMember()
        );

        // then
        assertEquals("DB save failed", exception.getMessage());
        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService).verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository).findById("ROLE_USER");
        verify(passwordEncoder).encode("password123");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("modify(): 회원 객체와 새 비밀번호가 주어지면 비밀번호를 인코딩하고 저장한 뒤 회원 ID를 반환한다")
    void shouldModifyMemberPasswordAndReturnMemberId_whenMemberAndPasswordProvided() {
        // given
        Member member = createMember(
                "testuser",
                "oldEncodedPassword",
                "홍길동",
                "test@example.com"
        );
        setMemberId(member, 1L);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("newEncodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        // when
        Long result = memberService.modify(member, "newPassword");

        // then
        assertEquals(1L, result);
        assertEquals("newEncodedPassword", member.getPassword());
        assertNotEquals("oldEncodedPassword", member.getPassword());
        assertNotEquals("newPassword", member.getPassword());

        verify(passwordEncoder).encode("newPassword");
        verify(memberRepository).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertEquals("newEncodedPassword", capturedMember.getPassword());
    }

    @Test
    @DisplayName("modify(): 저장 중 repository에서 예외가 발생하면 예외가 전파된다")
    void shouldPropagateException_whenRepositorySaveFailsInModify() {
        // given
        Member member = createMember(
                "testuser",
                "oldEncodedPassword",
                "홍길동",
                "test@example.com"
        );
        setMemberId(member, 1L);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("newEncodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new RuntimeException("DB save failed"));

        // when
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> memberService.modify(member, "newPassword")
        );

        // then
        assertEquals("DB save failed", exception.getMessage());
        verify(passwordEncoder).encode("newPassword");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("getMember(): username으로 회원을 조회한다")
    void shouldGetMemberByUsername_whenUsernameExists() {
        // given
        String username = "testuser";
        Member existingMember = createMember(
                username,
                "encodedPassword",
                "홍길동",
                "test@example.com"
        );

        when(memberRepository.findOneWithAuthoritiesByUsername(username))
                .thenReturn(Optional.of(existingMember));

        // when
        Member result = memberService.getMember(username);

        // then
        assertNotNull(result);
        assertSame(existingMember, result);
        verify(memberRepository).findOneWithAuthoritiesByUsername(username);
    }

    @Test
    @DisplayName("getMember(): 존재하지 않는 username이면 DataNotFoundException이 발생한다")
    void shouldThrowDataNotFoundException_whenUsernameNotFound() {
        // given
        String username = "testuser";

        when(memberRepository.findOneWithAuthoritiesByUsername(username))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> memberService.getMember(username)
        );

        // then
        assertEquals("회원을 찾을 수 없습니다.", exception.getMessage());
        verify(memberRepository).findOneWithAuthoritiesByUsername(username);
    }

    @Test
    @DisplayName("findUsername(): 이름과 이메일로 username을 찾는다")
    void shouldFindUsername_whenNameAndEmailMatch() {
        // given
        String name = "홍길동";
        String username = "testuser";
        String email = "test@example.com";

        Member existingMember = createMember(
                username,
                "encodedPassword",
                name,
                email
        );

        when(memberRepository.findByNameAndEmail(name, email))
                .thenReturn(Optional.of(existingMember));

        // when
        String result = memberService.findUsername(name, email);

        // then
        assertEquals(username, result);
        verify(memberRepository).findByNameAndEmail(name, email);
    }

    @Test
    @DisplayName("findUsername(): 일치하는 회원이 없으면 예외가 발생한다")
    void shouldThrowDataNotFoundException_whenNoMemberMatchesNameAndEmail() {
        // given
        String name = "홍길동";
        String email = "test@example.com";

        when(memberRepository.findByNameAndEmail(name, email))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> memberService.findUsername(name, email)
        );

        // then
        assertEquals("입력한 정보와 일치하는 회원을 찾을 수 없습니다.", exception.getMessage());
        verify(memberRepository).findByNameAndEmail(name, email);
    }

    @Test
    @DisplayName("createPasswordResetTokenAndSendEmail(): 일치하는 회원이 있으면 토큰을 저장하고 재설정 메일을 발송한다")
    void shouldCreatePasswordResetTokenAndSendEmail_whenMemberExists() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";
        String clientIp = "127.0.0.1";

        Member member = createMember(
                username,
                "oldEncodedPassword",
                name,
                email
        );

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.of(member));
        when(mailProperties.getPasswordResetExpirationSeconds()).thenReturn(1800L);

        // when
        memberService.createPasswordResetTokenAndSendEmail(name, email, username, clientIp);

        // then
        verify(mailRateLimitService).validatePasswordResetMailIpLimit(clientIp);
        verify(mailRateLimitService).validatePasswordResetMailCooldown(email);
        verify(mailRateLimitService).markPasswordResetMailCooldown(email);
        verify(passwordResetTokenRepository).save(eq("testuser"), anyString(), any(Duration.class));
        verify(mailService).sendPasswordResetEmail(eq(email), anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("createPasswordResetTokenAndSendEmail(): 일치하는 회원이 없으면 DataNotFoundException이 발생한다")
    void shouldThrowDataNotFoundException_whenNoMemberMatchesForPasswordReset() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";
        String clientIp = "127.0.0.1";

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> memberService.createPasswordResetTokenAndSendEmail(name, email, username, clientIp)
        );

        // then
        assertEquals("입력한 정보와 일치하는 회원을 찾을 수 없습니다.", exception.getMessage());
        verify(mailRateLimitService).validatePasswordResetMailIpLimit(clientIp);
        verify(mailRateLimitService, never()).validatePasswordResetMailCooldown(anyString());
        verify(passwordResetTokenRepository, never()).save(anyString(), anyString(), any());
        verify(memberRepository, never()).save(any(Member.class));
        verify(mailService, never()).sendPasswordResetEmail(eq(email), anyString());
    }

    @Test
    @DisplayName("createPasswordResetTokenAndSendEmail(): 메일 전송 실패 시 이전 토큰을 복구하고 예외를 전파한다")
    void shouldRestorePreviousTokenAndPropagateMailSendException_whenPasswordResetMailSendFails() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";
        String clientIp = "127.0.0.1";

        Member member = createMember(
                username,
                "oldEncodedPassword",
                name,
                email
        );

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.of(member));
        when(mailProperties.getPasswordResetExpirationSeconds()).thenReturn(1800L);
        when(passwordResetTokenRepository.getTokenByUsername("testuser")).thenReturn("previous-token");
        when(passwordResetTokenRepository.getExpirationByUsername("testuser")).thenReturn(Duration.ofSeconds(300));
        doThrow(new MailSendException("이메일 전송에 실패했습니다.", new RuntimeException("smtp error")))
                .when(mailService)
                .sendPasswordResetEmail(eq(email), anyString());

        // when
        MailSendException exception = assertThrows(
                MailSendException.class,
                () -> memberService.createPasswordResetTokenAndSendEmail(name, email, username, clientIp)
        );

        // then
        assertEquals("이메일 전송에 실패했습니다.", exception.getMessage());
        verify(mailRateLimitService).clearPasswordResetMailCooldown(email);
        verify(passwordResetTokenRepository, times(2)).save(eq("testuser"), anyString(), any(Duration.class));
        verify(passwordResetTokenRepository).save("testuser", "previous-token", Duration.ofSeconds(300));
        verify(mailService).sendPasswordResetEmail(eq(email), anyString());
    }

    @Test
    @DisplayName("getUsernameByPasswordResetToken(): 저장된 토큰이 있으면 사용자명을 반환한다")
    void shouldReturnUsername_whenPasswordResetTokenExists() {
        when(passwordResetTokenRepository.getUsernameByToken("valid-token")).thenReturn("testuser");

        String result = memberService.getUsernameByPasswordResetToken("valid-token");

        assertEquals("testuser", result);
        verify(passwordResetTokenRepository).getUsernameByToken("valid-token");
    }

    @Test
    @DisplayName("getUsernameByPasswordResetToken(): 토큰이 없으면 InvalidRequestException이 발생한다")
    void shouldThrowInvalidRequestException_whenPasswordResetTokenDoesNotExist() {
        when(passwordResetTokenRepository.getUsernameByToken("invalid-token")).thenReturn(null);

        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> memberService.getUsernameByPasswordResetToken("invalid-token")
        );

        assertEquals("유효하지 않거나 만료된 비밀번호 재설정 링크입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("resetPasswordWithToken(): 유효한 토큰이면 비밀번호를 변경하고 토큰을 삭제한다")
    void shouldResetPasswordWithToken_whenPasswordResetTokenIsValid() {
        Member member = createMember("testuser", "oldEncodedPassword", "홍길동", "test@example.com");
        setMemberId(member, 1L);

        when(passwordResetTokenRepository.getUsernameByToken("valid-token")).thenReturn("testuser");
        when(memberRepository.findOneWithAuthoritiesByUsername("testuser")).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        Long result = memberService.resetPasswordWithToken("valid-token", "newPassword123");

        assertEquals(1L, result);
        verify(passwordEncoder).encode("newPassword123");
        verify(memberRepository).save(member);
        verify(passwordResetTokenRepository).removeByToken("valid-token");
    }
}
