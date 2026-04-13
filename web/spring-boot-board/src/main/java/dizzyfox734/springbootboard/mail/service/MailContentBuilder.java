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

    public String buildPasswordResetContent(String resetLink) {
        return """
                <p>아래 링크를 눌러 새 비밀번호를 설정해주세요.</p>
                <br>
                <a href="%s">%s</a>
                <p>링크가 만료되었다면 비밀번호 찾기를 다시 요청해주세요.</p>
                """.formatted(resetLink, resetLink);
    }

    public String wrapAsHtml(String content) {
        return """
                <div style='margin:20px;'>
                    %s
                </div>
                """.formatted(content);
    }
}
