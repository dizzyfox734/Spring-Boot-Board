package dizzyfox734.springbootboard.service;

import jakarta.mail.MessagingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Test
    public void sendMailTest() {

        String to = "dizzyfox734@gmail.com";

        try {
            String checkcode = mailService.sendMail(to);
            System.out.println(">>>> checkcode: " + checkcode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
