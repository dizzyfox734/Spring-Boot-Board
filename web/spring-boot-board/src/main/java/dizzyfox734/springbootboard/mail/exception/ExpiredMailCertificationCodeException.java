package dizzyfox734.springbootboard.mail.exception;

public class ExpiredMailCertificationCodeException extends RuntimeException {
    public ExpiredMailCertificationCodeException() {
        super("인증코드가 없거나 만료되었습니다.");
    }
}
