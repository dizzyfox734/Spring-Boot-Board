package dizzyfox734.springbootboard.mail.service;

import dizzyfox734.springbootboard.mail.domain.MailProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @Mock
    private MailContentBuilder mailContentBuilder;

    @Mock
    private MailSenderService mailSenderService;

    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private MailService mailService;

    @Test
    @DisplayName("sendPasswordResetEmail(): 비밀번호 재설정 메일을 생성해 발송한다")
    public void shouldSendPasswordResetEmail_whenRequestIsValid() {
        // given
        String email = "test@example.com";
        String resetToken = "reset-token";
        String resetLink = "http://localhost:8080/member/reset/pwd/confirm?token=reset-token";
        String content = "test content";

        when(mailProperties.getPasswordResetBaseUrl()).thenReturn("http://localhost:8080");
        when(mailContentBuilder.buildPasswordResetContent(resetLink))
                .thenReturn(content);

        // when
        mailService.sendPasswordResetEmail(email, resetToken);

        // then
        verify(mailContentBuilder).buildPasswordResetContent(resetLink);
        verify(mailSenderService).send(email, "비밀번호 재설정 안내", content);
    }

    @Test
    @DisplayName("sendPasswordResetEmail(): 메일 본문 생성에 실패하면 발송하지 않는다")
    public void shouldPropagateExceptionAndStop_whenPasswordResetContentBuildFails() {
        // given
        String email = "test@example.com";
        String resetToken = "reset-token";
        String resetLink = "http://localhost:8080/member/reset/pwd/confirm?token=reset-token";

        when(mailProperties.getPasswordResetBaseUrl()).thenReturn("http://localhost:8080");
        when(mailContentBuilder.buildPasswordResetContent(resetLink))
                .thenThrow(new RuntimeException("비밀번호 재설정 본문 생성 실패"));

        // when
        assertThrows(RuntimeException.class,
                () -> mailService.sendPasswordResetEmail(email, resetToken));

        // then
        verify(mailContentBuilder).buildPasswordResetContent(resetLink);
        verify(mailSenderService, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("sendPasswordResetEmail(): 메일 발송에 실패하면 예외를 전파한다")
    public void shouldPropagateException_whenPasswordResetMailSendFails() {
        // given
        String email = "test@example.com";
        String resetToken = "reset-token";
        String resetLink = "http://localhost:8080/member/reset/pwd/confirm?token=reset-token";
        String content = "test content";

        when(mailProperties.getPasswordResetBaseUrl()).thenReturn("http://localhost:8080");
        when(mailContentBuilder.buildPasswordResetContent(resetLink))
                .thenReturn(content);

        doThrow(new RuntimeException("이메일 발송 실패"))
                .when(mailSenderService)
                .send(email, "비밀번호 재설정 안내", content);

        // when
        assertThrows(RuntimeException.class,
                () -> mailService.sendPasswordResetEmail(email, resetToken));

        // then
        verify(mailContentBuilder).buildPasswordResetContent(resetLink);
        verify(mailSenderService).send(email, "비밀번호 재설정 안내", content);
    }
}
