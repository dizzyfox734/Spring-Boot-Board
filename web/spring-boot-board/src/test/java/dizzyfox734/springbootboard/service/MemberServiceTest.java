package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.domain.member.Authority;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.domain.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}
