package dizzyfox734.springbootboard.member.controller.dto;

import dizzyfox734.springbootboard.global.validation.PasswordMatchable;
import dizzyfox734.springbootboard.global.validation.PasswordMatches;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordMatches
public class SignupDto implements PasswordMatchable {

    @Size(min = 3, max = 25, message = MemberValidationConstants.USERNAME_SIZE_MESSAGE)
    @NotEmpty(message = "회원ID는 필수항목입니다.")
    @Pattern(
            regexp = MemberValidationConstants.USERNAME_PATTERN,
            message = MemberValidationConstants.USERNAME_PATTERN_MESSAGE
    )
    private String username;

    @NotEmpty(message = "비밀번호는 필수항목입니다.")
    @Pattern(
            regexp = MemberValidationConstants.PASSWORD_PATTERN,
            message = MemberValidationConstants.PASSWORD_PATTERN_MESSAGE
    )
    private String password1;

    @NotEmpty(message = "비밀번호 확인은 필수항목입니다.")
    private String password2;

    @NotEmpty(message = "이름은 필수항목입니다.")
    private String name;

    @NotEmpty(message = "이메일은 필수항목입니다.")
    @Pattern(
            regexp = MemberValidationConstants.EMAIL_PATTERN,
            message = MemberValidationConstants.EMAIL_PATTERN_MESSAGE
    )
    private String email;

    @NotEmpty(message = "코드번호는 필수항목입니다.")
    private String emailConfirm;
}
