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

    /**
     * 8자리 랜덤 인증 코드 생성
     *
     * @return 생성된 인증 코드
     */
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

    /**
     * 이메일 전송 메시지 생성
     *
     * @param to 수신자 이메일 주소
     * @param content 이메일 내용
     * @param subject 이메일 제목
     * @return 생성된 MimeMessage 객체
     * @throws Exception 이메일 메시지 생성 중 발생할 수 있는 예외
     */
    private MimeMessage createMessage(String to, String content, String subject) throws Exception {
        MimeMessage message = javaMailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to);
        message.setSubject(subject);

        String msg = "";
        msg += "<div style='margin:20px;'>";
        msg+= content;
        msg+= "</div>";

        message.setText(msg, "utf-8", "html");
        message.setFrom(new InternetAddress("dizzyfox734@gmail.com", "dizzyfox734"));

        return message;
    }

    /**
     * 회원가입 인증 코드 이메일 전송
     *
     * @param to 수신자 이메일 주소
     * @throws Exception 이메일 전송 중 발생할 수 있는 예외
     */
    public void sendSignUpCheckCodeMail(String to) throws Exception {
        String checkcode = createKey();
        String content = "<p>아래 코드를 복사해 입력해주세요<p>";
        content+= "<br>";
        content+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        content+= "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        content+= "<div style='font-size:130%'>";
        content+= "CODE : <strong>";
        content+= checkcode + "</strong><div><br/>";
        MimeMessage message = createMessage(to, content, "회원가입 인증코드입니다.");

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

        mailCertificationDao.createMailCertification(to, checkcode);
    }

    /**
     * 임시 비밀번호 이메일로 전송
     *
     * @param to 이메일 수신자 주소
     * @param temporaryPassword 임시 비밀번호
     * @throws Exception 이메일 전송 중 발생할 수 있는 예외
     */
    public void sendTemporaryPasswordEmail(String to, String temporaryPassword) throws Exception {
        String subject = "임시 비밀번호 안내";
        String content = "임시 비밀번호는 <strong>" + temporaryPassword + "</strong>입니다.<br>로그인 후 반드시 비밀번호를 변경해주세요.";

        MimeMessage message = createMessage(to, content, subject);

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("이메일 전송 중 오류가 발생했습니다.");
        }
    }

    /**
     * 인증 코드가 일치하는지 확인
     *
     * @param email 인증을 수행할 이메일 주소
     * @param checkcode 입력한 인증 코드
     * @return 인증 결과 (true이면 인증 성공, false이면 인증 실패)
     */
    public boolean verifyMail(String email, String checkcode) {
        if (!isVerify(email, checkcode)) {
            return false;
        }
        mailCertificationDao.removeMailCertification(email);
        return true;
    }

    /**
     * 입력한 인증 코드가 유효한지 검사
     *
     * @param email 인증코드를 보낸 이메일 주소
     * @param checkcode 입력한 인증 코드
     * @return 유효한 인증 코드이면 true, 유효하지 않으면 false
     */
    private boolean isVerify(String email, String checkcode) {
        return mailCertificationDao.hasKey(email) &&
                mailCertificationDao.getMailCertification(email).equals(checkcode);
    }
}
