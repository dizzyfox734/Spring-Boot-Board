package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.domain.member.Authority;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.domain.member.MemberRepository;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
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
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("create(): 유효한 회원가입 정보가 주어지면 회원 객체를 생성하고 저장 요청 후 반환한다")
    public void shouldCreateMember_whenSignupRequestIsValid() {
        // given
        SignupDto dto = new SignupDto();
        dto.setUsername("testuser");
        dto.setPassword1("password123");
        dto.setPassword2("password123");
        dto.setName("홍길동");
        dto.setEmail("test@example.com");
        dto.setEmailConfirm("123456");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Member result = memberService.create(dto);

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("홍길동", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        assertNotEquals("password123", result.getPassword());
        assertTrue(result.isActivated());

        assertNotNull(result.getAuthorities());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(
                result.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_USER".equals(authority.getName()))
        );

        verify(passwordEncoder, times(1)).encode("password123");
        verify(memberRepository, times(1)).save(any(Member.class));

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertEquals("testuser", capturedMember.getUsername());
        assertEquals("홍길동", capturedMember.getName());
        assertEquals("test@example.com", capturedMember.getEmail());
        assertEquals("encodedPassword", capturedMember.getPassword());
        assertTrue(capturedMember.isActivated());
        assertTrue(
                capturedMember.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_USER".equals(authority.getName()))
        );
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

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new RuntimeException("DB save failed"));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> memberService.create(dto));

        assertEquals("DB save failed", exception.getMessage());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("modify(): 회원 객체와 새 비밀번호가 주어지면 비밀번호를 인코딩하고 저장한 뒤 반환한다")
    void shouldModifyMemberPassword_whenMemberAndPasswordProvided() {
        // given
        Member member = Member.builder()
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
        Member result = memberService.modify(member, "newPassword");

        // then
        assertNotNull(result);
        assertEquals("newEncodedPassword", result.getPassword());
        assertNotEquals("oldEncodedPassword", result.getPassword());
        assertNotEquals("newPassword", result.getPassword());

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

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> memberService.modify(member, "newPassword"));

        assertEquals("DB save failed", exception.getMessage());

        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    @DisplayName("validateDuplicateMember(): username이 존재하면 중복임을 반환한다")
    void shouldReturnDuplicated_whenUsernameExists() {
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
        boolean result = memberService.validateDuplicateMember(username);

        // then
        assertTrue(result);
        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername(username);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("validateDuplicateMember(): username이 존재하지 않으면 중복이 아님을 반환한다")
    void shouldReturnNotDuplicated_whenUsernameDoesNotExist() {
        // given
        String username = "testuser";
        when(memberRepository.findOneWithAuthoritiesByUsername(username))
                .thenReturn(Optional.empty());

        // when
        boolean result = memberService.validateDuplicateMember(username);

        // then
        assertFalse(result);
        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername(username);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("validateDuplicateEmail(): email이 존재하면 중복임을 반환한다")
    void shouldReturnDuplicated_whenEmailExists() {
        // given
        String email = "test@example.com";
        Authority authority = Authority.builder()
                .name("ROLE_USER")
                .build();
        Member existingMember = Member.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("홍길동")
                .email(email)
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();
        when(memberRepository.findOneWithAuthoritiesByEmail(email))
                .thenReturn(Optional.of(existingMember));

        // when
        boolean result = memberService.validateDuplicateEmail(email);

        // then
        assertTrue(result);
        verify(memberRepository, times(1)).findOneWithAuthoritiesByEmail(email);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("validateDuplicateEmail(): email이 존재하지 않으면 중복이 아님을 반환한다")
    void shouldReturnNotDuplicated_whenEmailDoesNotExist() {
        // given
        String email = "test@example.com";
        when(memberRepository.findOneWithAuthoritiesByEmail(email))
                .thenReturn(Optional.empty());

        // when
        boolean result = memberService.validateDuplicateEmail(email);

        // then
        assertFalse(result);
        verify(memberRepository, times(1)).findOneWithAuthoritiesByEmail(email);
        verify(memberRepository, never()).save(any(Member.class));
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
        assertSame(result, existingMember);
        verify(memberRepository, times(1)).findOneWithAuthoritiesByUsername(username);
    }

    @Test
    @DisplayName("getMember(): 존재하지 않는 username이면 DataNotFoundException이 발생한다")
    void shouldThrowException_whenUsernameNotFound() {
        // given
        String username = "testuser";

        when(memberRepository.findOneWithAuthoritiesByUsername(username))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> memberService.getMember(username));

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

        when(memberRepository.findByNameAndEmail(name, email)).thenReturn(Optional.of(existingMember));

        // when
        String result = memberService.findUsername(name, email);

        // then
        assertNotNull(result);
        assertEquals(username, result);

        verify(memberRepository, times(1)).findByNameAndEmail(name, email);
    }

    @Test
    @DisplayName("findUsername(): 일치하는 회원이 없으면 예외가 발생한다")
    void shouldThrowException_whenNoMemberMatchesNameAndEmail() {
        // given
        String name = "홍길동";
        String email = "test@example.com";

        when(memberRepository.findByNameAndEmail(name, email)).thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> memberService.findUsername(name, email));

        // then
        assertEquals("No user found with the provided name and email", exception.getMessage());

        verify(memberRepository, times(1)).findByNameAndEmail(name, email);
    }

    @Test
    @DisplayName("existEmail(): 이름, 이메일, 아이디가 모두 일치하면 회원이 존재함을 반환한다")
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

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username)).thenReturn(Optional.of(existingMember));

        // when
        boolean result = memberService.existEmail(name, email, username);

        // then
        assertTrue(result);

        verify(memberRepository, times(1)).findByNameAndEmailAndUsername(name, email, username);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("existEmail(): 일치하는 회원이 없으면 회원이 존재하지 않음을 반환한다")
    void shouldReturnNotExists_whenNoMemberMatchesAllFields() {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.empty());

        // when
        boolean result = memberService.existEmail(name, email, username);

        // then
        assertFalse(result);

        verify(memberRepository, times(1)).findByNameAndEmailAndUsername(name, email, username);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    @DisplayName("resetPasswordAndSendEmail(): 일치하는 회원이 있으면 임시 비밀번호를 생성하고 저장한 뒤 이메일로 전송한다")
    void shouldResetPasswordAndSendTemporaryPasswordEmail_whenMemberExists() throws Exception {
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
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
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
    @DisplayName("resetPasswordAndSendEmail(): 일치하는 회원이 없으면 예외가 발생한다")
    void shouldThrowException_whenNoMemberMatchesForPasswordReset() throws Exception {
        // given
        String name = "홍길동";
        String email = "test@example.com";
        String username = "testuser";

        when(memberRepository.findByNameAndEmailAndUsername(name, email, username))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> memberService.resetPasswordAndSendEmail(name, email, username));

        // then
        assertEquals("No user found with the provided name and email", exception.getMessage());

        verify(passwordEncoder, never()).encode(any(String.class));
        verify(memberRepository, never()).save(any(Member.class));
        verify(mailService, never()).sendTemporaryPasswordEmail(eq(email), any(String.class));
    }

    @Test
    @DisplayName("resetPasswordAndSendEmail(): 이메일 전송 중 예외가 발생하면 예외가 전파된다")
    void shouldPropagateException_whenTemporaryPasswordEmailSendFails() throws Exception {
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
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IllegalArgumentException("이메일 전송 중 오류가 발생했습니다."))
                .when(mailService)
                .sendTemporaryPasswordEmail(eq(email), any(String.class));

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> memberService.resetPasswordAndSendEmail(name, email, username));

        // then
        assertEquals("이메일 전송 실패", exception.getMessage());
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        assertEquals("이메일 전송 중 오류가 발생했습니다.", exception.getCause().getMessage());

        verify(passwordEncoder, times(1)).encode(any(String.class));
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(mailService, times(1)).sendTemporaryPasswordEmail(eq(email), any(String.class));
    }
}
