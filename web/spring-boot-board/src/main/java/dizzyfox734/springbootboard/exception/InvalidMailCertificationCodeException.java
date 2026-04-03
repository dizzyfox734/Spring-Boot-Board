package dizzyfox734.springbootboard.exception;

public class InvalidMailCertificationCodeException extends RuntimeException {
    public InvalidMailCertificationCodeException() {
        super("인증코드가 올바르지 않습니다.");
    }
}
