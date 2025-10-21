package dizzyfox734.springbootboard.controller.dto;

import jakarta.validation.constraints.AssertTrue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterAgreementDto {

    @AssertTrue(message = "회원가입약관에 동의해야 합니다.")
    private boolean agree1;

    @AssertTrue(message = "개인정보처리방침에 동의해야 합니다.")
    private boolean agree2;
}

