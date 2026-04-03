package dizzyfox734.springbootboard.domain.mail;

import dizzyfox734.springbootboard.exception.MailMessageBuildException;
import dizzyfox734.springbootboard.exception.MailSendException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class SmtpMailSenderService implements MailSenderService {

    private final JavaMailSender javaMailSender;
    private final MailContentBuilder mailContentBuilder;
    private final MailProperties mailProperties;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public SmtpMailSenderService(JavaMailSender javaMailSender,
                                 MailContentBuilder mailContentBuilder,
                                 MailProperties mailProperties) {
        this.javaMailSender = javaMailSender;
        this.mailContentBuilder = mailContentBuilder;
        this.mailProperties = mailProperties;
    }

    @Override
    public void send(String to, String subject, String content) {
        MimeMessage message = createMessage(to, subject, content);

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new MailSendException("이메일 전송에 실패했습니다.", e);
        }
    }

    private MimeMessage createMessage(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.addRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            message.setText(mailContentBuilder.wrapAsHtml(content), "utf-8", "html");
            message.setFrom(new InternetAddress(fromEmail, mailProperties.getSenderName()));
            return message;
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new MailMessageBuildException("이메일 메시지 생성에 실패했습니다.", e);
        }
    }
}
