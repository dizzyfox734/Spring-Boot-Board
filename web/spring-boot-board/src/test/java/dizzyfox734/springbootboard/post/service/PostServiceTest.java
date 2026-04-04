package dizzyfox734.springbootboard.post.service;

import dizzyfox734.springbootboard.global.exception.AccessDeniedException;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.exception.InvalidInputException;
import dizzyfox734.springbootboard.global.exception.InvalidRequestException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    private Member createMember(String username) {
        return Member.builder()
                .username(username)
                .build();
    }

    private Post createPost(String username) {
        Post post = new Post();
        post.setId(1);
        post.setTitle("oldTitle");
        post.setContent("oldContent");
        post.setAuthor(createMember(username));
        return post;
    }

    @Test
    @DisplayName("getPost(): 존재하는 게시글이면 게시글을 반환한다")
    void shouldReturnPost_whenPostExistsOnGetPost() {
        // given
        Post existingPost = createPost("testuser");

        when(postRepository.findById(1))
                .thenReturn(Optional.of(existingPost));

        // when
        Post result = postService.getPost(1);

        // then
        assertNotNull(result);
        assertSame(existingPost, result);
        verify(postRepository).findById(1);
    }

    @Test
    @DisplayName("getPost(): 존재하지 않는 게시글이면 예외가 발생한다")
    void shouldThrowException_whenPostNotFound() {
        when(postRepository.findById(1))
                .thenReturn(Optional.empty());

        DataNotFoundException ex = assertThrows(
                DataNotFoundException.class,
                () -> postService.getPost(1)
        );

        assertEquals("Post not found", ex.getMessage());
    }

    @Test
    @DisplayName("create(): 정상 입력이면 게시글 저장 후 ID 반환")
    void shouldCreatePostAndReturnId() {
        Member member = createMember("testuser");

        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post saved = invocation.getArgument(0);
                    saved.setId(1);
                    return saved;
                });

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        Integer result = postService.create("title", "content", member);

        assertEquals(1, result);

        verify(postRepository).save(captor.capture());

        Post saved = captor.getValue();
        assertEquals("title", saved.getTitle());
        assertEquals("content", saved.getContent());
        assertEquals(member, saved.getAuthor());
    }

    @Test
    @DisplayName("create(): 제목 null이면 예외")
    void shouldThrow_whenTitleNull() {
        assertThrows(InvalidInputException.class,
                () -> postService.create(null, "content", createMember("testuser")));

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("create(): 내용 blank면 예외")
    void shouldThrow_whenContentBlank() {
        assertThrows(InvalidInputException.class,
                () -> postService.create("title", "   ", createMember("testuser")));

        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("modify(): 작성자 본인이면 수정 후 save 호출 + ID 반환")
    void shouldModifyAndSavePost_whenAuthorMatches() {
        Post post = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);

        Integer result = postService.modify(1, "newTitle", "newContent", "testuser");

        assertEquals(1, result);

        verify(postRepository).findById(1);
        verify(postRepository).save(captor.capture());

        Post saved = captor.getValue();
        assertEquals("newTitle", saved.getTitle());
        assertEquals("newContent", saved.getContent());
    }

    @Test
    @DisplayName("modify(): username null이면 예외")
    void shouldThrow_whenUsernameNull() {
        Post post = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        InvalidRequestException ex = assertThrows(
                InvalidRequestException.class,
                () -> postService.modify(1, "t", "c", null)
        );

        assertEquals("Username is null or blank", ex.getMessage());
    }

    @Test
    @DisplayName("modify(): 작성자가 아니면 예외")
    void shouldThrow_whenNotAuthor() {
        Post post = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        assertThrows(AccessDeniedException.class,
                () -> postService.modify(1, "t", "c", "wronguser"));
    }

    @Test
    @DisplayName("delete(): 작성자 본인이면 삭제 + ID 반환")
    void shouldDeletePost_whenAuthorMatches() {
        Post post = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        Integer result = postService.delete(1, "testuser");

        assertEquals(1, result);

        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("delete(): 작성자가 아니면 예외")
    void shouldThrow_whenDeleteByNonAuthor() {
        Post post = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        assertThrows(AccessDeniedException.class,
                () -> postService.delete(1, "wronguser"));
    }

    @Test
    @DisplayName("getPostForModify(): 작성자 본인이면 반환")
    void shouldReturnPost_whenAuthorMatchesForModify() {
        Post post = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        Post result = postService.getPostForModify(1, "testuser");

        assertSame(post, result);
    }

    @Test
    @DisplayName("getPostForModify(): 작성자가 아니면 예외")
    void shouldThrow_whenNotAuthorForGetPostForModify() {
        Post post = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(post));

        assertThrows(AccessDeniedException.class,
                () -> postService.getPostForModify(1, "wronguser"));
    }
}
