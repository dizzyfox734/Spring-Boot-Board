package dizzyfox734.springbootboard.member.controller.dto;

public final class MemberValidationConstants {

    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9]+$";
    public static final String USERNAME_PATTERN_MESSAGE = "아이디는 영문, 숫자만 가능합니다.";
    public static final String USERNAME_SIZE_MESSAGE = "아이디는 3 ~ 25자리까지 가능합니다.";

    public static final String PASSWORD_PATTERN =
            "^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}$";
    public static final String PASSWORD_PATTERN_MESSAGE =
            "비밀번호는 영문과 숫자 조합으로 8 ~ 16자리까지 가능합니다.";

    public static final String EMAIL_PATTERN =
            "^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,3}$";
    public static final String EMAIL_PATTERN_MESSAGE = "올바르지 않은 이메일 형식입니다.";

    private MemberValidationConstants() {
    }
}
