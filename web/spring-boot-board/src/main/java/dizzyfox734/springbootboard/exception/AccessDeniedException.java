package dizzyfox734.springbootboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "access denied")
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public AccessDeniedException(String message) {
        super(message);
    }
}
