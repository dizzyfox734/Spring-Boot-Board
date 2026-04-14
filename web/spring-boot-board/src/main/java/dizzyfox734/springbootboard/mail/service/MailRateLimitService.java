package dizzyfox734.springbootboard.mail.service;

import dizzyfox734.springbootboard.mail.domain.MailProperties;
import dizzyfox734.springbootboard.mail.exception.TooManyMailRequestException;
import dizzyfox734.springbootboard.mail.repository.MailRateLimitRepository;
import org.springframework.stereotype.Service;

@Service
public class MailRateLimitService {

    private final MailRateLimitRepository mailRateLimitRepository;
    private final MailProperties mailProperties;

    public MailRateLimitService(MailRateLimitRepository mailRateLimitRepository,
                                MailProperties mailProperties) {
        this.mailRateLimitRepository = mailRateLimitRepository;
        this.mailProperties = mailProperties;
    }

    public void validateSignupMailRequest(String email, String clientIp) {
        validateSignupMailIpLimit(clientIp);
        validateSignupMailCooldown(email);
    }

    public void validateSignupMailCooldown(String email) {
        if (mailRateLimitRepository.existsSignupEmailCooldown(email)) {
            throw new TooManyMailRequestException();
        }
    }

    public void validateSignupMailIpLimit(String clientIp) {
        validateIpRequestCount(mailRateLimitRepository.incrementSignupIpRequestCount(clientIp));
    }

    public void markSignupMailCooldown(String email) {
        mailRateLimitRepository.saveSignupEmailCooldown(email);
    }

    public void clearSignupMailCooldown(String email) {
        mailRateLimitRepository.removeSignupEmailCooldown(email);
    }

    public void validatePasswordResetMailIpLimit(String clientIp) {
        validateIpRequestCount(mailRateLimitRepository.incrementPasswordResetIpRequestCount(clientIp));
    }

    public void validatePasswordResetMailCooldown(String email) {
        if (mailRateLimitRepository.existsPasswordResetEmailCooldown(email)) {
            throw new TooManyMailRequestException();
        }
    }

    public void markPasswordResetMailCooldown(String email) {
        mailRateLimitRepository.savePasswordResetEmailCooldown(email);
    }

    public void clearPasswordResetMailCooldown(String email) {
        mailRateLimitRepository.removePasswordResetEmailCooldown(email);
    }

    private void validateIpRequestCount(long count) {
        if (count > mailProperties.getIpLimitMaxRequests()) {
            throw new TooManyMailRequestException();
        }
    }
}
