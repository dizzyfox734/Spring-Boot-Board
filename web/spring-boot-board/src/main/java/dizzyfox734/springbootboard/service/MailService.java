package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.controller.dao.MailCertificationDao;
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;
    private final MailCertificationDao mailCertificationDao;

    @Autowired
    public MailService(MailCertificationDao mailCertificationDao) {
        this.mailCertificationDao = mailCertificationDao;
    }

    private static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3);

            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97)); // a~z
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65)); // A~Z
                    break;
                case 2:
                    key.append(rnd.nextInt(10)); // 0~9
                    break;
            }
        }

        return key.toString();
    }

    private MimeMessage createMessage(String to, String checkcode) throws Exception {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to);
        message.setSubject("회원가입 인증코드입니다.");

        String msg = "";
        msg += "<div style='margin:20px;'>";
        msg+= "<p>아래 코드를 복사해 입력해주세요<p>";
        msg+= "<br>";
        msg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msg+= "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        msg+= "<div style='font-size:130%'>";
        msg+= "CODE : <strong>";
        msg+= checkcode + "</strong><div><br/> ";
        msg+= "</div>";

        message.setText(msg, "utf-8", "html");
        message.setFrom(new InternetAddress("dizzyfox734@gmail.com", "dizzyfox734"));

        return message;
    }

    public void sendMail(String to) throws Exception {
        String checkcode = createKey();
        MimeMessage message = createMessage(to, checkcode);

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

        mailCertificationDao.createMailCertification(to, checkcode);
    }

    public boolean verifyMail(String email, String checkcode) {
        if (isVerify(email, checkcode)) {
            return false;
        }
        mailCertificationDao.removeMailCertification(email);
        return true;
    }

    private boolean isVerify(String email, String checkcode) {
        return !(mailCertificationDao.hasKey(email)
                && mailCertificationDao.getMailCertification(email)
                .equals(checkcode));
    }
}
