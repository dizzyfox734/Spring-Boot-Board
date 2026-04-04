package dizzyfox734.springbootboard.member.service;

import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.mail.exception.ExpiredMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.exception.InvalidMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.exception.MailMessageBuildException;
import dizzyfox734.springbootboard.mail.exception.MailSendException;
import dizzyfox734.springbootboard.mail.service.MailCertificationService;
import dizzyfox734.springbootboard.mail.service.MailService;
import dizzyfox734.springbootboard.member.controller.dto.SignupDto;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.exception.AuthorityNotFoundException;
import dizzyfox734.springbootboard.member.exception.DuplicateEmailException;
import dizzyfox734.springbootboard.member.exception.DuplicateUsernameException;
import dizzyfox734.springbootboard.member.exception.EmailVerificationException;
import dizzyfox734.springbootboard.member.exception.PasswordMismatchException;
import dizzyfox734.springbootboard.member.repository.AuthorityRepository;
import dizzyfox734.springbootboard.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
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

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("create(): 유효한 회원가입 정보가 주어지면 회원을 생성하고 저장한 뒤 회원 ID를 반환한다")
    void shouldCreateMemberAndReturnMemberId_whenCreateRequestIsValid() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("password123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

        Authority roleUser = Authority.builder()
                .name("ROLE_USER")
                .build();

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
                    savedMember.setId(1L);
                    return savedMember;
                });

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);

        // when
        Long result = memberService.create(dto);

        // then
        assertNotNull(result);
        assertEquals(1L, result);

        verify(memberRepository, times(1))
                .findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository, times(1))
                .findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService, times(1))
                .verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository, times(1))
                .findById("ROLE_USER");
        verify(passwordEncoder, times(1))
                .encode("password123");
        verify(memberRepository, times(1))
                .save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertEquals("testuser", capturedMember.getUsername());
        assertEquals("홍길동", capturedMember.getName());
        assertEquals("test@example.com", capturedMember.getEmail());
        assertEquals("encodedPassword", capturedMember.getPassword());
        assertNotEquals("password123", capturedMember.getPassword());
        assertTrue(capturedMember.isActivated());
        assertNotNull(capturedMember.getAuthorities());
        assertEquals(1, capturedMember.getAuthorities().size());
        assertTrue(capturedMember.getAuthorities().contains(roleUser));
    }

    @Test
    @DisplayName("create(): 비밀번호 확인이 일치하지 않으면 PasswordMismatchException이 발생한다")
    void shouldThrowPasswordMismatchException_whenPasswordsDoNotMatch() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("wrongpassword123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

        // when
        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> memberService.create(dto)
        );

        // then
        assertEquals("패스워드가 일치하지 않습니다.", exception.getMessage());

        verify(memberRepository, never()).findOneWithAuthoritiesByUsername(anyString());
        verify(memberRepository, never()).findOneWithAuthoritiesByEmail(anyString());
        verify(mailCertificationService, never()).verifyEmailCertificationCode(anyString(), anyString());
        verify(authorityRepository, never()).findById(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): username이 중복되면 DuplicateUsernameException이 발생한다")
    void shouldThrowDuplicateUsernameException_whenUsernameAlreadyExists() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("password123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

        Member existingMember = Member.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("기존회원")
                .email("exist@example.com")
                .activated(true)
                .build();

        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.of(existingMember));

        // when
        DuplicateUsernameException exception = assertThrows(
                DuplicateUsernameException.class,
                () -> memberService.create(dto)
        );

        // then
        assertEquals("이미 등록된 아이디입니다.", exception.getMessage());

        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername("testuser");
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
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("password123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

        Member existingMember = Member.builder()
                .username("existingMember")
                .password("encodedPassword")
                .name("기존회원")
                .email("test@example.com")
                .activated(true)
                .build();

        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.of(existingMember));

        // when
        DuplicateEmailException exception = assertThrows(
                DuplicateEmailException.class,
                () -> memberService.create(dto)
        );

        // then
        assertEquals("이미 등록된 이메일입니다.", exception.getMessage());

        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository, times(1)).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService, never()).verifyEmailCertificationCode(anyString(), anyString());
        verify(authorityRepository, never()).findById(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): 인증코드가 일치하지 않으면 EmailVerificationException이 발생한다")
    void shouldThrowEmailVerificationException_whenCertificationCodeIsInvalid() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("password123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

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
                () -> memberService.create(dto)
        );

        // then
        assertEquals("인증코드가 올바르지 않습니다.", exception.getMessage());

        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository, times(1)).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService, times(1))
                .verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository, never()).findById(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): 인증코드가 없거나 만료되면 EmailVerificationException이 발생한다")
    void shouldThrowEmailVerificationException_whenCertificationCodeIsExpired() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("password123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

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
                () -> memberService.create(dto)
        );

        // then
        assertEquals("인증코드가 없거나 만료되었습니다.", exception.getMessage());

        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository, times(1)).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService, times(1))
                .verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository, never()).findById(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): 기본 권한 조회에 실패하면 AuthorityNotFoundException이 발생한다")
    void shouldThrowAuthorityNotFoundException_whenDefaultAuthorityIsMissing() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("password123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(authorityRepository.findById("ROLE_USER"))
                .thenReturn(Optional.empty());

        // when
        AuthorityNotFoundException exception = assertThrows(
                AuthorityNotFoundException.class,
                () -> memberService.create(dto)
        );

        // then
        assertEquals("ROLE_USER 권한이 존재하지 않습니다.", exception.getMessage());

        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository, times(1)).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService, times(1))
                .verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository, times(1)).findById("ROLE_USER");
        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("create(): 저장 중 repository에서 예외가 발생하면 예외가 전파된다")
    void shouldPropagateException_whenRepositorySaveFailsInCreate() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("password123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

        Authority roleUser = Authority.builder()
                .name("ROLE_USER")
                .build();

        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());
        when(memberRepository.findOneWithAuthoritiesByEmail("test@example.com"))
                .thenReturn(Optional.empty());
        when(authorityRepository.findById("ROLE_USER"))
                .thenReturn(Optional.of(roleUser));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new RuntimeException("DB save failed"));

        // when
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> memberService.create(dto)
        );

        // then
        assertEquals("DB save failed", exception.getMessage());
        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername("testuser");
        verify(memberRepository, times(1)).findOneWithAuthoritiesByEmail("test@example.com");
        verify(mailCertificationService, times(1))
                .verifyEmailCertificationCode("test@example.com", "123456");
        verify(authorityRepository, times(1)).findById("ROLE_USER");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("modify(): 회원 객체와 새 비밀번호가 주어지면 비밀번호를 인코딩하고 저장한 뒤 회원 ID를 반환한다")
    void shouldModifyMemberPasswordAndReturnMemberId_whenMemberAndPasswordProvided() {
        // given
        Member member = Member.builder()
                .id(1L)
                .username("testuser")
                .name("홍길동")
                .email("test@example.com")
                .password("oldEncodedPassword")
                .authorities(Set.of(new Authority("ROLE_USER")))
                .activated(true)
                .build();

        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Long result = memberService.modify(member, "newPassword");

        // then
        assertNotNull(result);
        assertEquals(1L, result);
        assertEquals("newEncodedPassword", member.getPassword());
        assertNotEquals("oldEncodedPassword", member.getPassword());
        assertNotEquals("newPassword", member.getPassword());

        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(memberRepository, times(1)).save(any(Member.class));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertEquals("newEncodedPassword", capturedMember.getPassword());
    }

    @Test
    @DisplayName("modify(): 저장 중 repository에서 예외가 발생하면 예외가 전파된다")
    void shouldPropagateException_whenRepositorySaveFailsInModify() {
        // given
        Member member = Member.builder()
                .id(1L)
                .username("testuser")
                .name("홍길동")
                .email("test@example.com")
                .password("oldEncodedPassword")
                .authorities(Set.of(new Authority("ROLE_USER")))
                .activated(true)
                .build();

        when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new RuntimeException("DB save failed"));

        // when
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> memberService.modify(member, "newPassword")
        );

        // then
        assertEquals("DB save failed", exception.getMessage());

        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("getMember(): username으로 회원을 조회한다")
    void shouldGetMemberByUsername_whenUsernameExists() {
        // given
        String username = "testuser";
        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();
        Member existingMember = Member.builder()
                .username(username)
                .password("encodedPassword")
                .name("홍길동")
                .email("test@example.com")
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        when(memberRepository.findOneWithAuthoritiesByUsername(username))
                .thenReturn(Optional.of(existingMember));

        // when
        Member result = memberService.getMember(username);

        // then
        assertNotNull(result);
        assertSame(existingMember, result);
        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername(username);
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
        assertEquals("user not found", exception.getMessage());
        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername(username);
    }

    @Test
    @DisplayName("findUsername(): 이름과 이메일로 username을 찾는다")
    void shouldFindUsername_whenNameAndEmailMatch() {
        // given
        String name = "홍길동";
        String username = "testuser";
        String email = "test@example.com";
        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();
        Member existingMember = Member.builder()
                .username(username)
                .password("encodedPassword")
                .name(name)
                .email(email)
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        when(memberRepository.findByNameAndEmail(name, email))
                .thenReturn(Optional.of(existingMember));

        // when
        String result = memberService.findUsername(name, email);

        // then
        assertNotNull(result);
        assertEquals(username, result);

        verify(memberRepository, times(1)).findByNameAndEmail(name, email);
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
        assertEquals("No user found with the provided name and email", exception.getMessage());
        verify(memberRepository, times(1)).findByNameAndEmail(name, email);
    }

    @Test
    @DisplayName("existsForPasswordReset(): 이름, 이메일, 아이디가 모두 일치하면 회원이 존재함을 반환한다")
    void shouldReturnExists_whenNameEmailAndUsernameMatch() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";
        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();
        Member existingMember = Member.builder()
                .name(name)
                .email(email)
                .username(username)
                .password("encodedPassword")
                .activated(true)
                .authorities(Collections.singleton(authority))
                .build();

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.of(existingMember));

        // when
        boolean result = memberService.existsForPasswordReset(name, email, username);

        // then
        assertTrue(result);
        verify(memberRepository, times(1))
                .findByNameAndEmailAndUsername(name, email, username);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("existsForPasswordReset(): 일치하는 회원이 없으면 회원이 존재하지 않음을 반환한다")
    void shouldReturnNotExists_whenNoMemberMatchesAllFields() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.empty());

        // when
        boolean result = memberService.existsForPasswordReset(name, email, username);

        // then
        assertFalse(result);
        verify(memberRepository, times(1))
                .findByNameAndEmailAndUsername(name, email, username);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("resetPasswordAndSendEmail(): 일치하는 회원이 있으면 임시 비밀번호를 생성하고 저장한 뒤 이메일로 전송한다")
    void shouldResetPasswordAndSendTemporaryPasswordEmail_whenMemberExists() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";
        String oldPassword = "oldPassword";

        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();
        Member member = Member.builder()
                .username(username)
                .password(oldPassword)
                .name(name)
                .email(email)
                .activated(true)
                .authorities(Collections.singleton(authority))
                .build();

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.of(member));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        String result = memberService.resetPasswordAndSendEmail(name, email, username);

        // then
        assertNotNull(result);
        assertFalse(result.isBlank());

        verify(passwordEncoder, times(1)).encode(result);
        verify(memberRepository, times(1)).save(member);
        verify(mailService, times(1)).sendTemporaryPasswordEmail(email, result);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());

        Member savedMember = captor.getValue();
        assertEquals("encodedPassword", savedMember.getPassword());
    }

    @Test
    @DisplayName("resetPasswordAndSendEmail(): 일치하는 회원이 없으면 DataNotFoundException이 발생한다")
    void shouldThrowDataNotFoundException_whenNoMemberMatchesForPasswordReset() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> memberService.resetPasswordAndSendEmail(name, email, username)
        );

        // then
        assertEquals("No user found with the provided name and email", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(memberRepository, never()).save(any(Member.class));
        verify(mailService, never()).sendTemporaryPasswordEmail(eq(email), anyString());
    }

    @Test
    @DisplayName("resetPasswordAndSendEmail(): 메일 전송 실패 시 MailSendException이 전파된다")
    void shouldPropagateMailSendException_whenTemporaryPasswordEmailSendFails() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";
        String oldPassword = "oldPassword";

        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();
        Member member = Member.builder()
                .username(username)
                .password(oldPassword)
                .name(name)
                .email(email)
                .activated(true)
                .authorities(Collections.singleton(authority))
                .build();

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.of(member));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new MailSendException("이메일 전송에 실패했습니다.", new RuntimeException("smtp error")))
                .when(mailService)
                .sendTemporaryPasswordEmail(eq(email), anyString());

        // when
        MailSendException exception = assertThrows(
                MailSendException.class,
                () -> memberService.resetPasswordAndSendEmail(name, email, username)
        );

        // then
        assertEquals("이메일 전송에 실패했습니다.", exception.getMessage());

        verify(passwordEncoder, times(1)).encode(anyString());
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(mailService, times(1)).sendTemporaryPasswordEmail(eq(email), anyString());
    }

    @Test
    @DisplayName("resetPasswordAndSendEmail(): 메시지 생성 실패 시 MailMessageBuildException이 전파된다")
    void shouldPropagateMailMessageBuildException_whenTemporaryPasswordMessageBuildFails() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";
        String oldPassword = "oldPassword";

        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();
        Member member = Member.builder()
                .username(username)
                .password(oldPassword)
                .name(name)
                .email(email)
                .activated(true)
                .authorities(Collections.singleton(authority))
                .build();

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.of(member));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new MailMessageBuildException("이메일 메시지 생성에 실패했습니다.", new RuntimeException("mime error")))
                .when(mailService)
                .sendTemporaryPasswordEmail(eq(email), anyString());

        // when
        MailMessageBuildException exception = assertThrows(
                MailMessageBuildException.class,
                () -> memberService.resetPasswordAndSendEmail(name, email, username)
        );

        // then
        assertEquals("이메일 메시지 생성에 실패했습니다.", exception.getMessage());

        verify(passwordEncoder, times(1)).encode(anyString());
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(mailService, times(1)).sendTemporaryPasswordEmail(eq(email), anyString());
    }
}
