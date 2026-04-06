package dizzyfox734.springbootboard.comment.domain;

import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.post.domain.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

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

    private Post createPost(String username) {
        return Post.create("게시글 제목", "게시글 내용", createMember(username));
    }

    @Test
    @DisplayName("create(): 유효한 값이 주어지면 댓글을 생성한다")
    void shouldCreateComment_whenValidArgumentsAreGiven() {
        // given
        Post post = createPost("postwriter");
        Member author = createMember("commentwriter");

        // when
        Comment comment = Comment.create("댓글 내용", post, author);

        // then
        assertNotNull(comment);
        assertEquals("댓글 내용", comment.getContent());
        assertEquals(post, comment.getPost());
        assertEquals(author, comment.getAuthor());
    }

    @Test
    @DisplayName("create(): content가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenContentIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Comment.create(null, createPost("postwriter"), createMember("commentwriter"))
        );

        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): content가 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenContentIsBlank() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Comment.create("   ", createPost("postwriter"), createMember("commentwriter"))
        );

        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): post가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenPostIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Comment.create("댓글 내용", null, createMember("commentwriter"))
        );

        assertEquals("게시글 정보는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): author가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenAuthorIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Comment.create("댓글 내용", createPost("postwriter"), null)
        );

        assertEquals("작성자는 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("edit(): 유효한 값이 주어지면 댓글 내용을 수정한다")
    void shouldEditComment_whenValidContentIsGiven() {
        // given
        Comment comment = Comment.create(
                "old content",
                createPost("postwriter"),
                createMember("commentwriter")
        );

        // when
        comment.edit("new content");

        // then
        assertEquals("new content", comment.getContent());
    }

    @Test
    @DisplayName("edit(): content가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenEditContentIsNull() {
        // given
        Comment comment = Comment.create(
                "old content",
                createPost("postwriter"),
                createMember("commentwriter")
        );

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comment.edit(null)
        );

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("edit(): content가 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenEditContentIsBlank() {
        // given
        Comment comment = Comment.create(
                "old content",
                createPost("postwriter"),
                createMember("commentwriter")
        );

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> comment.edit("   ")
        );

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("isWrittenBy(): 작성자 username과 같으면 true를 반환한다")
    void shouldReturnTrue_whenUsernameMatchesAuthor() {
        // given
        Comment comment = Comment.create(
                "댓글 내용",
                createPost("postwriter"),
                createMember("commentwriter")
        );

        // when
        boolean result = comment.isWrittenBy("commentwriter");

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("isWrittenBy(): 작성자 username과 다르면 false를 반환한다")
    void shouldReturnFalse_whenUsernameDoesNotMatchAuthor() {
        // given
        Comment comment = Comment.create(
                "댓글 내용",
                createPost("postwriter"),
                createMember("commentwriter")
        );

        // when
        boolean result = comment.isWrittenBy("wronguser");

        // then
        assertFalse(result);
    }
}
