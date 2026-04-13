package dizzyfox734.springbootboard.mail.domain;

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

    /**
     * 비밀번호 재설정 토큰 만료 시간(초)
     */
    private long passwordResetExpirationSeconds = 1800L;

    /**
     * 비밀번호 재설정 링크 생성에 사용할 기본 URL
     */
    private String passwordResetBaseUrl = "http://localhost:8080";
}
