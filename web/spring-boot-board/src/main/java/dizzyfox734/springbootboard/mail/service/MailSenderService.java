package dizzyfox734.springbootboard.mail.service;

public interface MailSenderService {
    void send(String to, String subject, String content);
}
