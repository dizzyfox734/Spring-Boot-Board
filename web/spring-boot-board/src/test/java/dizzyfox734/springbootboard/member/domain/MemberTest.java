package dizzyfox734.springbootboard.member.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MemberTest {

    private Authority createAuthority() {
        return Authority.builder()
                .name("ROLE_USER")
                .build();
    }

    @Test
    @DisplayName("create(): 유효한 값이 주어지면 회원을 생성한다")
    void shouldCreateMember_whenValidArgumentsAreGiven() {
        // when
        Member member = Member.create(
                "testuser",
                "encodedPassword",
                "홍길동",
                "test@example.com",
                Set.of(createAuthority())
        );

        // then
        assertNotNull(member);
        assertEquals("testuser", member.getUsername());
        assertEquals("encodedPassword", member.getPassword());
        assertEquals("홍길동", member.getName());
        assertEquals("test@example.com", member.getEmail());
        assertNotNull(member.getAuthorities());
        assertEquals(1, member.getAuthorities().size());
    }

    @Test
    @DisplayName("create(): username이 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenUsernameIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        null,
                        "encodedPassword",
                        "홍길동",
                        "test@example.com",
                        Set.of(createAuthority())
                )
        );

        assertEquals("아이디는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): username이 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenUsernameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "   ",
                        "encodedPassword",
                        "홍길동",
                        "test@example.com",
                        Set.of(createAuthority())
                )
        );

        assertEquals("아이디는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): encodedPassword가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenEncodedPasswordIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "testuser",
                        null,
                        "홍길동",
                        "test@example.com",
                        Set.of(createAuthority())
                )
        );

        assertEquals("비밀번호는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): encodedPassword가 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenEncodedPasswordIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "testuser",
                        "   ",
                        "홍길동",
                        "test@example.com",
                        Set.of(createAuthority())
                )
        );

        assertEquals("비밀번호는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): name이 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenNameIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "testuser",
                        "encodedPassword",
                        null,
                        "test@example.com",
                        Set.of(createAuthority())
                )
        );

        assertEquals("이름은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): name이 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenNameIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "testuser",
                        "encodedPassword",
                        "   ",
                        "test@example.com",
                        Set.of(createAuthority())
                )
        );

        assertEquals("이름은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): email이 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenEmailIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "testuser",
                        "encodedPassword",
                        "홍길동",
                        null,
                        Set.of(createAuthority())
                )
        );

        assertEquals("이메일은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): email이 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenEmailIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "testuser",
                        "encodedPassword",
                        "홍길동",
                        "   ",
                        Set.of(createAuthority())
                )
        );

        assertEquals("이메일은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): authorities가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenAuthoritiesIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "testuser",
                        "encodedPassword",
                        "홍길동",
                        "test@example.com",
                        null
                )
        );

        assertEquals("권한은 최소 하나 이상 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): authorities가 비어 있으면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenAuthoritiesIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Member.create(
                        "testuser",
                        "encodedPassword",
                        "홍길동",
                        "test@example.com",
                        Set.of()
                )
        );

        assertEquals("권한은 최소 하나 이상 필요합니다.", exception.getMessage());
    }

    @Test
    @DisplayName("changeEncodedPassword(): 유효한 비밀번호로 변경한다")
    void shouldChangeEncodedPassword_whenValidPasswordIsGiven() {
        // given
        Member member = Member.create(
                "testuser",
                "oldEncodedPassword",
                "홍길동",
                "test@example.com",
                Set.of(createAuthority())
        );

        // when
        member.changeEncodedPassword("newEncodedPassword");

        // then
        assertEquals("newEncodedPassword", member.getPassword());
    }

    @Test
    @DisplayName("changeEncodedPassword(): null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenChangingPasswordToNull() {
        // given
        Member member = Member.create(
                "testuser",
                "oldEncodedPassword",
                "홍길동",
                "test@example.com",
                Set.of(createAuthority())
        );

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> member.changeEncodedPassword(null)
        );

        // then
        assertEquals("비밀번호는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("changeEncodedPassword(): 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenChangingPasswordToBlank() {
        // given
        Member member = Member.create(
                "testuser",
                "oldEncodedPassword",
                "홍길동",
                "test@example.com",
                Set.of(createAuthority())
        );

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> member.changeEncodedPassword("   ")
        );

        // then
        assertEquals("비밀번호는 필수입니다.", exception.getMessage());
    }
}
