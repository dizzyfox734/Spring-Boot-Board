package dizzyfox734.springbootboard.mail.service;

import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final String TEMPORARY_PASSWORD_MAIL_SUBJECT = "임시 비밀번호 안내";

    private final MailSenderService mailSenderService;
    private final MailContentBuilder mailContentBuilder;

    public MailService(MailSenderService mailSenderService,
                       MailContentBuilder mailContentBuilder) {
        this.mailSenderService = mailSenderService;
        this.mailContentBuilder = mailContentBuilder;
    }

    public void sendTemporaryPasswordEmail(String to, String temporaryPassword) {
        String content = mailContentBuilder.buildTemporaryPasswordContent(temporaryPassword);
        mailSenderService.send(to, TEMPORARY_PASSWORD_MAIL_SUBJECT, content);
    }
}
