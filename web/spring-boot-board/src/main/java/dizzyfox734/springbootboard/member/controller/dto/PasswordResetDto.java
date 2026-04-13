package dizzyfox734.springbootboard.member.controller.dto;

import dizzyfox734.springbootboard.global.validation.PasswordMatchable;
import dizzyfox734.springbootboard.global.validation.PasswordMatches;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordMatches
public class PasswordResetDto implements PasswordMatchable {

    @NotEmpty(message = "토큰 정보가 없습니다.")
    private String token;

    @NotEmpty(message = "비밀번호는 필수항목입니다.")
    @Pattern(
            regexp = "^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}",
            message = "비밀번호는 영문과 숫자 조합으로 8 ~ 16자리까지 가능합니다."
    )
    private String password1;

    @NotEmpty(message = "비밀번호 확인은 필수항목입니다.")
    private String password2;
}
