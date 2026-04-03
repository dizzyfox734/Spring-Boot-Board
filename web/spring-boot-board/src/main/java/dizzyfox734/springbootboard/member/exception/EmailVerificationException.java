package dizzyfox734.springbootboard.member.exception;

public class EmailVerificationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public EmailVerificationException(String message) {
        super(message);
    }
}
