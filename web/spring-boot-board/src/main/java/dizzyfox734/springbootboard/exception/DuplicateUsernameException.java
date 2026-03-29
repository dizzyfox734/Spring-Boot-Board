package dizzyfox734.springbootboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class DuplicateUsernameException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
