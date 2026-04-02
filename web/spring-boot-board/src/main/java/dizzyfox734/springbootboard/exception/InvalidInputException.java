package dizzyfox734.springbootboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid post input")
public class InvalidInputException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public InvalidInputException(String message) {
        super(message);
    }
}
