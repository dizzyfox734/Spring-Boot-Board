package dizzyfox734.springbootboard.member.controller.dto;

import dizzyfox734.springbootboard.global.validation.PasswordMatchable;
import dizzyfox734.springbootboard.global.validation.PasswordMatches;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@PasswordMatches
public class MemberModifyDto implements PasswordMatchable {

    @NotEmpty(message = "비밀번호는 필수항목입니다.")
    private String password1;

    @NotEmpty(message = "비밀번호 확인은 필수항목입니다.")
    private String password2;
}
