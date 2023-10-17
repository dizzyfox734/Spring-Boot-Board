package dizzyfox734.springbootboard.exception;

public class MailAuthenticationFailedException extends RuntimeException{

    public MailAuthenticationFailedException() {
        super();
    }
    public MailAuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    public MailAuthenticationFailedException(String message) {
        super(message);
    }
    public MailAuthenticationFailedException(Throwable cause) {
        super(cause);
    }
}
