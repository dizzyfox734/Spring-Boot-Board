package dizzyfox734.springbootboard.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "access denied")
public class PostAccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public PostAccessDeniedException(String message) {
        super(message);
    }
}
