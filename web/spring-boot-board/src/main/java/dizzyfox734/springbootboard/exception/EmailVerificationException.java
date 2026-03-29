package dizzyfox734.springbootboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class EmailVerificationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public EmailVerificationException(String message) {
        super(message);
    }
}
