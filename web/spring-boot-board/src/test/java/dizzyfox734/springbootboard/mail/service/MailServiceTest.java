package dizzyfox734.springbootboard.mail.service;

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

    @InjectMocks
    private MailService mailService;

    @Test
    @DisplayName("sendTemporaryPasswordEmail(): 임시 비밀번호 메일을 생성해 발송한다")
    public void shouldSendTemporaryPasswordEmail_whenRequestIsValid() {
        // given
        String email = "test@example.com";
        String temporaryPassword = "temporarypassword";
        String content = "test content";

        when(mailContentBuilder.buildTemporaryPasswordContent(temporaryPassword))
                .thenReturn(content);

        // when
        mailService.sendTemporaryPasswordEmail(email, temporaryPassword);

        // then
        verify(mailContentBuilder, times(1)).buildTemporaryPasswordContent(temporaryPassword);
        verify(mailSenderService, times(1)).send(email, "임시 비밀번호 안내", content);
    }

    @Test
    @DisplayName("sendTemporaryPasswordEmail(): 임시 비밀번호 본문 생성에 실패하면 발송하지 않는다")
    public void shouldPropagateExceptionAndStop_whenTemporaryPasswordContentBuildFails() {
        // given
        String email = "test@example.com";
        String temporaryPassword = "temporarypassword";

        when(mailContentBuilder.buildTemporaryPasswordContent(temporaryPassword))
                .thenThrow(new RuntimeException("임시 비밀번호 생성 실패"));

        // when
        assertThrows(RuntimeException.class,
                () -> mailService.sendTemporaryPasswordEmail(email, temporaryPassword));

        // then
        verify(mailContentBuilder, times(1)).buildTemporaryPasswordContent(temporaryPassword);
        verify(mailSenderService, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("sendTemporaryPasswordEmail(): 메일 발송에 실패하면 예외를 전파한다")
    public void shouldPropagateException_whenTemporaryPasswordMailSendFails() {
        // given
        String email = "test@example.com";
        String temporaryPassword = "temporarypassword";
        String content = "test content";

        when(mailContentBuilder.buildTemporaryPasswordContent(temporaryPassword))
                .thenReturn(content);

        doThrow(new RuntimeException("이메일 발송 실패"))
                .when(mailSenderService)
                .send(email, "임시 비밀번호 안내", content);

        // when
        assertThrows(RuntimeException.class,
                () -> mailService.sendTemporaryPasswordEmail(email, temporaryPassword));

        // then
        verify(mailContentBuilder, times(1)).buildTemporaryPasswordContent(temporaryPassword);
        verify(mailSenderService, times(1)).send(email, "임시 비밀번호 안내", content);
    }
}
