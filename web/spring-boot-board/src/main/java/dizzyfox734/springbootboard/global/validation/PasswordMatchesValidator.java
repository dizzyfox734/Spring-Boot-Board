package dizzyfox734.springbootboard.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, PasswordMatchable> {

    @Override
    public boolean isValid(PasswordMatchable target, ConstraintValidatorContext context) {
        if (target == null) {
            return true;
        }

        boolean valid = target.getPassword1() != null
                && target.getPassword2() != null
                && target.getPassword1().equals(target.getPassword2());

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호가 일치하지 않습니다.")
                    .addPropertyNode("password2")
                    .addConstraintViolation();
        }

        return valid;
    }
}
