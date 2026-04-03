package dizzyfox734.springbootboard.mail.service;

import dizzyfox734.springbootboard.mail.domain.CertificationCodeGenerator;
import dizzyfox734.springbootboard.mail.exception.ExpiredMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.exception.InvalidMailCertificationCodeException;
import dizzyfox734.springbootboard.mail.repository.MailCertificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailCertificationServiceTest {

    @Mock
    private MailCertificationRepository mailCertificationRepository;

    @Mock
    private CertificationCodeGenerator certificationCodeGenerator;

    @Mock
    private MailContentBuilder mailContentBuilder;

    @Mock
    private MailSenderService mailSenderService;

    @InjectMocks
    private MailCertificationService mailCertificationService;

    @Test
    @DisplayName("sendSignupVerificationCode(): 인증코드를 생성하고 메일 본문을 만든 뒤 발송 후 저장한다")
    public void shouldSendSignupVerificationCodeAndSaveCertificationCode_whenEmailIsValid() {
        // given
        String email = "test@example.com";
        String certificationCode = "A1B2C3D4";
        String content = "test content";

        when(certificationCodeGenerator.generate()).thenReturn(certificationCode);
        when(mailContentBuilder.buildSignUpVerificationContent(certificationCode))
                .thenReturn(content);

        // when
        mailCertificationService.sendSignupVerificationCode(email);

        // then
        verify(certificationCodeGenerator).generate();
        verify(mailContentBuilder).buildSignUpVerificationContent(certificationCode);
        verify(mailSenderService).send(email, "회원가입 인증코드입니다.", content);
        verify(mailCertificationRepository).save(email, certificationCode);
    }

    @Test
    @DisplayName("sendSignupVerificationCode(): 메일 발송에 실패하면 인증코드를 저장하지 않는다")
    public void shouldNotSaveCertificationCode_whenMailSendFails() {
        // given
        String email = "test@example.com";
        String certificationCode = "A1B2C3D4";
        String content = "test content";

        when(certificationCodeGenerator.generate()).thenReturn(certificationCode);
        when(mailContentBuilder.buildSignUpVerificationContent(certificationCode))
                .thenReturn(content);

        doThrow(new RuntimeException("이메일 발송 실패"))
                .when(mailSenderService)
                .send(email, "회원가입 인증코드입니다.", content);

        // when & then
        assertThrows(RuntimeException.class,
                () -> mailCertificationService.sendSignupVerificationCode(email)
        );

        verify(mailCertificationRepository, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("sendSignupVerificationCode(): 인증코드 생성에 실패하면 이후 작업을 수행하지 않는다")
    public void shouldPropagateExceptionAndStop_whenCodeGenerationFails() {
        // given
        String email = "test@example.com";

        when(certificationCodeGenerator.generate())
                .thenThrow(new IllegalStateException("인증코드 생성 실패"));

        // when
        assertThrows(IllegalStateException.class,
                () -> mailCertificationService.sendSignupVerificationCode(email));

        // then
        verify(mailContentBuilder, never()).buildSignUpVerificationContent(anyString());
        verify(mailSenderService, never()).send(anyString(), anyString(), anyString());
        verify(mailCertificationRepository, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("sendSignupVerificationCode(): 메일 본문 생성에 실패하면 발송과 저장을 수행하지 않는다")
    public void shouldPropagateExceptionAndStop_whenMailContentBuildFails() {
        // given
        String email = "test@example.com";
        String certificationCode = "A1B2C3D4";

        when(certificationCodeGenerator.generate()).thenReturn(certificationCode);
        when(mailContentBuilder.buildSignUpVerificationContent(certificationCode))
                .thenThrow(new RuntimeException("본문 생성 실패"));

        // when
        assertThrows(RuntimeException.class,
                () -> mailCertificationService.sendSignupVerificationCode(email));

        // then
        verify(certificationCodeGenerator, times(1)).generate();
        verify(mailContentBuilder, times(1)).buildSignUpVerificationContent(certificationCode);
        verify(mailSenderService, never()).send(anyString(), anyString(), anyString());
        verify(mailCertificationRepository, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("verifyEmailCertificationCode(): 저장된 인증코드와 일치하면 인증 처리 후 코드를 삭제한다")
    public void shouldRemoveCertificationCode_whenVerificationCodeMatches() {
        // given
        String email = "test@example.com";
        String certificationCode = "A1B2C3D4";

        when(mailCertificationRepository.get(email))
                .thenReturn(certificationCode);

        // when
        mailCertificationService.verifyEmailCertificationCode(email, certificationCode);

        // then
        verify(mailCertificationRepository).get(email);
        verify(mailCertificationRepository).remove(email);
    }

    @Test
    @DisplayName("verifyEmailCertificationCode(): 저장된 인증코드가 없으면 만료 예외를 던진다")
    public void shouldThrowExpiredException_whenCertificationCodeDoesNotExist() {
        // given
        String email = "test@example.com";
        String certificationCode = "A1B2C3D4";

        when(mailCertificationRepository.get(email)).thenReturn(null);

        // when
        ExpiredMailCertificationCodeException exception = assertThrows(ExpiredMailCertificationCodeException.class,
                () -> mailCertificationService.verifyEmailCertificationCode(email, certificationCode));

        // then
        assertEquals("인증코드가 없거나 만료되었습니다.", exception.getMessage());

        verify(mailCertificationRepository, times(1)).get(email);
        verify(mailCertificationRepository, never()).remove(email);
    }

    @Test
    @DisplayName("verifyEmailCertificationCode(): 저장된 인증코드와 다르면 불일치 예외를 던진다")
    public void shouldThrowInvalidCodeException_whenCertificationCodeDoesNotMatch() {
        // given
        String email = "test@example.com";
        String certificationCode = "A1B2C3D4";
        String wrongCertificationCode = "w1r2o3n4g5";

        when(mailCertificationRepository.get(email)).thenReturn(wrongCertificationCode);

        // when
        InvalidMailCertificationCodeException exception = assertThrows(InvalidMailCertificationCodeException.class,
                () -> mailCertificationService.verifyEmailCertificationCode(email, certificationCode));

        // then
        assertEquals("인증코드가 올바르지 않습니다.", exception.getMessage());

        verify(mailCertificationRepository, times(1)).get(email);
        verify(mailCertificationRepository, never()).remove(email);
    }

    @Test
    @DisplayName("verifyEmailCertificationCode(): 인증 성공 후 삭제에 실패하면 예외를 전파한다")
    public void shouldPropagateException_whenRemovingCertificationCodeFails() {
        // given
        String email = "test@example.com";
        String certificationCode = "A1B2C3D4";

        when(mailCertificationRepository.get(email)).thenReturn(certificationCode);

        doThrow(new RuntimeException("인증코드 삭제 실패"))
                .when(mailCertificationRepository)
                .remove(email);

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> mailCertificationService.verifyEmailCertificationCode(email, certificationCode));

        // then
        assertEquals("인증코드 삭제 실패", exception.getMessage());
        verify(mailCertificationRepository).get(email);
        verify(mailCertificationRepository).remove(email);
    }
}
