package dizzyfox734.springbootboard.domain.mail;

public interface MailSenderService {
    void send(String to, String subject, String content);
}
