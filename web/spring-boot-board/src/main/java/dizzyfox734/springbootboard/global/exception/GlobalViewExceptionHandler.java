package dizzyfox734.springbootboard.global.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalViewExceptionHandler {

    @ExceptionHandler(DataNotFoundException.class)
    public String handleDataNotFound(DataNotFoundException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(InvalidRequestException.class)
    public String handleInvalidRequest(InvalidRequestException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(InvalidInputException.class)
    public String handleInvalidInput(InvalidInputException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/403";
    }
}