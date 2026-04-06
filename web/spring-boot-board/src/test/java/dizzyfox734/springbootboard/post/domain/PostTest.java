package dizzyfox734.springbootboard.post.domain;

import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    private Member createMember(String username) {
        return Member.create(
                username,
                "encodedPassword",
                "홍길동",
                username + "@example.com",
                Set.of(
                        Authority.builder()
                                .name("ROLE_USER")
                                .build()
                )
        );
    }

    @Test
    @DisplayName("create(): 유효한 값이 주어지면 게시글을 생성한다")
    void shouldCreatePost_whenValidArgumentsAreGiven() {
        // given
        Member author = createMember("testuser");

        // when
        Post post = Post.create("제목", "내용", author);

        // then
        assertNotNull(post);
        assertEquals("제목", post.getTitle());
        assertEquals("내용", post.getContent());
        assertEquals(author, post.getAuthor());
    }

    @Test
    @DisplayName("create(): title이 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenTitleIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Post.create(null, "내용", createMember("testuser"))
        );

        assertEquals("제목은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): title이 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenTitleIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Post.create("   ", "내용", createMember("testuser"))
        );

        assertEquals("제목은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): content가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenContentIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Post.create("제목", null, createMember("testuser"))
        );

        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): content가 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenContentIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Post.create("제목", "   ", createMember("testuser"))
        );

        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): author가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenAuthorIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Post.create("제목", "내용", null)
        );

        assertEquals("작성자는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("edit(): 유효한 값이 주어지면 제목과 내용을 수정한다")
    void shouldEditPost_whenValidArgumentsAreGiven() {
        // given
        Post post = Post.create("old title", "old content", createMember("testuser"));

        // when
        post.edit("new title", "new content");

        // then
        assertEquals("new title", post.getTitle());
        assertEquals("new content", post.getContent());
    }

    @Test
    @DisplayName("edit(): title이 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenEditTitleIsNull() {
        // given
        Post post = Post.create("old title", "old content", createMember("testuser"));

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> post.edit(null, "new content")
        );

        // then
        assertEquals("제목은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("edit(): content가 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenEditContentIsBlank() {
        // given
        Post post = Post.create("old title", "old content", createMember("testuser"));

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> post.edit("new title", "   ")
        );

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("isWrittenBy(): 작성자 username과 같으면 true를 반환한다")
    void shouldReturnTrue_whenUsernameMatchesAuthor() {
        // given
        Post post = Post.create("제목", "내용", createMember("testuser"));

        // when
        boolean result = post.isWrittenBy("testuser");

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("isWrittenBy(): 작성자 username과 다르면 false를 반환한다")
    void shouldReturnFalse_whenUsernameDoesNotMatchAuthor() {
        // given
        Post post = Post.create("제목", "내용", createMember("testuser"));

        // when
        boolean result = post.isWrittenBy("wronguser");

        // then
        assertFalse(result);
    }
}
