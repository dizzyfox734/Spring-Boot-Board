package dizzyfox734.springbootboard.mail.service;

import org.springframework.stereotype.Component;

@Component
public class MailContentBuilder {

    public String buildSignUpVerificationContent(String certificationCode) {
        return """
                <p>아래 코드를 복사해 입력해주세요.</p>
                <br>
                <div align='center' style='border:1px solid black; font-family:verdana;'>
                    <h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>
                    <div style='font-size:130%%'>
                        CODE : <strong>%s</strong>
                    </div>
                </div>
                """.formatted(certificationCode);
    }

    public String buildTemporaryPasswordContent(String temporaryPassword) {
        return """
                임시 비밀번호는 <strong>%s</strong>입니다.<br>
                로그인 후 반드시 비밀번호를 변경해주세요.
                """.formatted(temporaryPassword);
    }

    public String wrapAsHtml(String content) {
        return """
                <div style='margin:20px;'>
                    %s
                </div>
                """.formatted(content);
    }
}
