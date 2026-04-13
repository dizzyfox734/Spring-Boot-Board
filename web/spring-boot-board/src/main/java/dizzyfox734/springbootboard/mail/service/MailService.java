package dizzyfox734.springbootboard.mail.service;

import dizzyfox734.springbootboard.mail.domain.MailProperties;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final String PASSWORD_RESET_MAIL_SUBJECT = "비밀번호 재설정 안내";

    private final MailSenderService mailSenderService;
    private final MailContentBuilder mailContentBuilder;
    private final MailProperties mailProperties;

    public MailService(MailSenderService mailSenderService,
                       MailContentBuilder mailContentBuilder,
                       MailProperties mailProperties) {
        this.mailSenderService = mailSenderService;
        this.mailContentBuilder = mailContentBuilder;
        this.mailProperties = mailProperties;
    }

    public void sendPasswordResetEmail(String to, String resetToken) {
        String resetLink = buildPasswordResetLink(resetToken);
        String content = mailContentBuilder.buildPasswordResetContent(resetLink);
        mailSenderService.send(to, PASSWORD_RESET_MAIL_SUBJECT, content);
    }

    private String buildPasswordResetLink(String resetToken) {
        String baseUrl = mailProperties.getPasswordResetBaseUrl();
        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        return normalizedBaseUrl + "/member/reset/pwd/confirm?token=" + resetToken;
    }
}
