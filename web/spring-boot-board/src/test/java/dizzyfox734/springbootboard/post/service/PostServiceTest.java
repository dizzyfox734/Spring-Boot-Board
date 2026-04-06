package dizzyfox734.springbootboard.post.service;

import dizzyfox734.springbootboard.global.exception.AccessDeniedException;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.exception.InvalidRequestException;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.post.domain.Post;
import dizzyfox734.springbootboard.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Authority createAuthority() {
        return Authority.builder()
                .name("ROLE_USER")
                .build();
    }

    private Member createMember(String username) {
        return Member.create(
                username,
                "encodedPassword",
                "홍길동",
                username + "@example.com",
                Set.of(createAuthority())
        );
    }

    private Post createPost(String username) {
        return Post.create("oldTitle", "oldContent", createMember(username));
    }

    private void setPostId(Post post, Integer id) {
        try {
            Field field = Post.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(post, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("getPost(): 존재하는 게시글이면 게시글을 반환한다")
    void shouldReturnPost_whenPostExistsOnGetPost() {
        // given
        Post existingPost = createPost("testuser");
        setPostId(existingPost, 1);

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        Post result = postService.getPost(1);

        // then
        assertNotNull(result);
        assertSame(existingPost, result);
        verify(postRepository).findById(1);
    }

    @Test
    @DisplayName("getPost(): 존재하지 않는 게시글이면 예외가 발생한다")
    void shouldThrowDataNotFoundException_whenPostNotFound() {
        // given
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        // when
        DataNotFoundException ex = assertThrows(
                DataNotFoundException.class,
                () -> postService.getPost(1)
        );

        // then
        assertEquals("Post not found", ex.getMessage());
        verify(postRepository).findById(1);
    }

    @Test
    @DisplayName("getPost(): postId가 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenPostIdIsNullOnGetPost() {
        // when
        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> postService.getPost(null)
        );

        // then
        assertEquals("Post id is null", ex.getMessage());
        verify(postRepository, never()).findById(any());
    }

    @Test
    @DisplayName("create(): 정상 입력이면 게시글 저장 후 ID 반환")
    void shouldCreatePostAndReturnId() {
        // given
        Member member = createMember("testuser");

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post saved = invocation.getArgument(0);
                    setPostId(saved, 1);
                    return saved;
                });

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        // when
        Integer result = postService.create("title", "content", member);

        // then
        assertEquals(1, result);
        verify(postRepository).save(captor.capture());

        Post saved = captor.getValue();
        assertEquals("title", saved.getTitle());
        assertEquals("content", saved.getContent());
        assertEquals(member, saved.getAuthor());
    }

    @Test
    @DisplayName("create(): 제목이 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenTitleIsNullOnCreate() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> postService.create(null, "content", createMember("testuser"))
        );

        assertEquals("제목은 필수항목입니다.", ex.getMessage());
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("create(): 내용이 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenContentIsBlankOnCreate() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> postService.create("title", "   ", createMember("testuser"))
        );

        assertEquals("내용은 필수항목입니다.", ex.getMessage());
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("create(): 작성자가 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenAuthorIsNullOnCreate() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> postService.create("title", "content", null)
        );

        assertEquals("작성자는 필수입니다.", ex.getMessage());
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("modify(): 작성자 본인이면 수정 후 저장하고 ID를 반환한다")
    void shouldModifyAndSavePost_whenAuthorMatches() {
        // given
        Post post = createPost("testuser");
        setPostId(post, 1);

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        // when
        Integer result = postService.modify(1, "newTitle", "newContent", "testuser");

        // then
        assertEquals(1, result);
        verify(postRepository).findById(1);
        verify(postRepository).save(captor.capture());

        Post saved = captor.getValue();
        assertEquals("newTitle", saved.getTitle());
        assertEquals("newContent", saved.getContent());
    }

    @Test
    @DisplayName("modify(): postId가 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenPostIdIsNullOnModify() {
        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> postService.modify(null, "t", "c", "testuser")
        );

        assertEquals("Post id is null", ex.getMessage());
        verify(postRepository, never()).findById(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("modify(): username이 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsNullOnModify() {
        // given
        Post post = createPost("testuser");
        setPostId(post, 1);
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when
        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> postService.modify(1, "t", "c", null)
        );

        // then
        assertEquals("Username is null or blank", ex.getMessage());
        verify(postRepository).findById(1);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("modify(): 작성자가 아니면 예외가 발생한다")
    void shouldThrowAccessDeniedException_whenRequesterIsNotAuthorOnModify() {
        // given
        Post post = createPost("testuser");
        setPostId(post, 1);
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when & then
        assertThrows(AccessDeniedException.class,
                () -> postService.modify(1, "t", "c", "wronguser"));

        verify(postRepository).findById(1);
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete(): 작성자 본인이면 삭제 후 ID를 반환한다")
    void shouldDeletePost_whenAuthorMatches() {
        // given
        Post post = createPost("testuser");
        setPostId(post, 1);

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when
        Integer result = postService.delete(1, "testuser");

        // then
        assertEquals(1, result);
        verify(postRepository).findById(1);
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("delete(): postId가 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenPostIdIsNullOnDelete() {
        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> postService.delete(null, "testuser")
        );

        assertEquals("Post id is null", ex.getMessage());
        verify(postRepository, never()).findById(any());
        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("delete(): 작성자가 아니면 예외가 발생한다")
    void shouldThrowAccessDeniedException_whenDeleteByNonAuthor() {
        // given
        Post post = createPost("testuser");
        setPostId(post, 1);
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when & then
        assertThrows(AccessDeniedException.class,
                () -> postService.delete(1, "wronguser"));

        verify(postRepository).findById(1);
        verify(postRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getPostForModify(): 작성자 본인이면 게시글을 반환한다")
    void shouldReturnPost_whenAuthorMatchesForGetPostForModify() {
        // given
        Post post = createPost("testuser");
        setPostId(post, 1);
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when
        Post result = postService.getPostForModify(1, "testuser");

        // then
        assertSame(post, result);
        verify(postRepository).findById(1);
    }

    @Test
    @DisplayName("getPostForModify(): 작성자가 아니면 예외가 발생한다")
    void shouldThrowAccessDeniedException_whenNotAuthorForGetPostForModify() {
        // given
        Post post = createPost("testuser");
        setPostId(post, 1);
        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        // when & then
        assertThrows(AccessDeniedException.class,
                () -> postService.getPostForModify(1, "wronguser"));

        verify(postRepository).findById(1);
    }
}
