package dizzyfox734.springbootboard.mail.domain;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RandomCertificationCodeGenerator implements CertificationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final MailProperties mailProperties;

    public RandomCertificationCodeGenerator(MailProperties mailProperties) {
        this.mailProperties = mailProperties;
    }

    @Override
    public String generate() {
        StringBuilder key = new StringBuilder();
        int length = mailProperties.getCertificationCodeLength();

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(3);

            switch (index) {
                case 0 -> key.append((char) (RANDOM.nextInt(26) + 97)); // a-z
                case 1 -> key.append((char) (RANDOM.nextInt(26) + 65)); // A-Z
                case 2 -> key.append(RANDOM.nextInt(10));               // 0-9
                default -> throw new IllegalStateException("인증코드 생성 중 잘못된 분기입니다.");
            }
        }

        return key.toString();
    }
}
