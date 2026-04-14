package dizzyfox734.springbootboard.mail.service;

import dizzyfox734.springbootboard.mail.domain.MailProperties;
import dizzyfox734.springbootboard.mail.exception.TooManyMailRequestException;
import dizzyfox734.springbootboard.mail.repository.MailRateLimitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailRateLimitServiceTest {

    @Mock
    private MailRateLimitRepository mailRateLimitRepository;

    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private MailRateLimitService mailRateLimitService;

    @Test
    @DisplayName("validateSignupMailRequest(): signup 이메일 cooldown이 존재하면 예외가 발생한다")
    void shouldThrowTooManyMailRequestException_whenSignupEmailCooldownExists() {
        when(mailRateLimitRepository.incrementSignupIpRequestCount("127.0.0.1")).thenReturn(1L);
        when(mailProperties.getIpLimitMaxRequests()).thenReturn(5L);
        when(mailRateLimitRepository.existsSignupEmailCooldown("test@example.com")).thenReturn(true);

        assertThrows(TooManyMailRequestException.class,
                () -> mailRateLimitService.validateSignupMailRequest("test@example.com", "127.0.0.1"));
    }

    @Test
    @DisplayName("validateSignupMailRequest(): signup IP 요청 횟수가 초과되면 예외가 발생한다")
    void shouldThrowTooManyMailRequestException_whenSignupIpLimitExceeded() {
        when(mailRateLimitRepository.incrementSignupIpRequestCount("127.0.0.1")).thenReturn(6L);
        when(mailProperties.getIpLimitMaxRequests()).thenReturn(5L);

        assertThrows(TooManyMailRequestException.class,
                () -> mailRateLimitService.validateSignupMailRequest("test@example.com", "127.0.0.1"));
    }

    @Test
    @DisplayName("validatePasswordResetMailIpLimit(): 비밀번호 재설정 IP 요청 횟수가 초과되면 예외가 발생한다")
    void shouldThrowTooManyMailRequestException_whenPasswordResetIpLimitExceeded() {
        when(mailRateLimitRepository.incrementPasswordResetIpRequestCount("127.0.0.1")).thenReturn(6L);
        when(mailProperties.getIpLimitMaxRequests()).thenReturn(5L);

        assertThrows(TooManyMailRequestException.class,
                () -> mailRateLimitService.validatePasswordResetMailIpLimit("127.0.0.1"));
    }

    @Test
    @DisplayName("validatePasswordResetMailCooldown(): 비밀번호 재설정 이메일 cooldown이 존재하면 예외가 발생한다")
    void shouldThrowTooManyMailRequestException_whenPasswordResetEmailCooldownExists() {
        when(mailRateLimitRepository.existsPasswordResetEmailCooldown("test@example.com")).thenReturn(true);

        assertThrows(TooManyMailRequestException.class,
                () -> mailRateLimitService.validatePasswordResetMailCooldown("test@example.com"));
    }

    @Test
    @DisplayName("mark/clear 메서드들은 각 용도에 맞는 repository를 호출한다")
    void shouldDelegateCooldownOperationsToRepository() {
        mailRateLimitService.markSignupMailCooldown("signup@example.com");
        mailRateLimitService.clearSignupMailCooldown("signup@example.com");
        mailRateLimitService.markPasswordResetMailCooldown("reset@example.com");
        mailRateLimitService.clearPasswordResetMailCooldown("reset@example.com");

        verify(mailRateLimitRepository).saveSignupEmailCooldown("signup@example.com");
        verify(mailRateLimitRepository).removeSignupEmailCooldown("signup@example.com");
        verify(mailRateLimitRepository).savePasswordResetEmailCooldown("reset@example.com");
        verify(mailRateLimitRepository).removePasswordResetEmailCooldown("reset@example.com");
    }
}
