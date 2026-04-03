package dizzyfox734.springbootboard.domain.mail;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    /**
     * 발신자 이름
     */
    private String senderName = "SBB";

    /**
     * 인증코드 길이
     */
    private int certificationCodeLength = 8;

    /**
     * 인증코드 만료 시간(초)
     */
    private long certificationExpirationSeconds = 600L;
}
