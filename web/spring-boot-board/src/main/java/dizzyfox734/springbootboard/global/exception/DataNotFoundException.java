package dizzyfox734.springbootboard.global.exception;

public class DataNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    public DataNotFoundException(String message) {
        super(message);
    }
}
