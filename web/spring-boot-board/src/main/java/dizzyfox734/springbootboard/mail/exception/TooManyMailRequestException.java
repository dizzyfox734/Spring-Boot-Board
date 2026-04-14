package dizzyfox734.springbootboard.mail.exception;

public class TooManyMailRequestException extends RuntimeException {
    public TooManyMailRequestException() {
        super("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
    }
}
