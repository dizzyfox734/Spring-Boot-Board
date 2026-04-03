package dizzyfox734.springbootboard.member.exception;

public class DuplicateUsernameException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public DuplicateUsernameException(String message) {
        super(message);
    }
}
