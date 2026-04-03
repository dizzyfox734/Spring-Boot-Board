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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

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
    @DisplayName("findOne(): 존재하는 게시글이면 게시글을 반환한다")
    void shouldReturnPost_whenPostExistsOnFindOne() {
        // given
        Post existingPost = new Post();
        existingPost.setId(1);
        existingPost.setTitle("test title");
        existingPost.setContent("test content");
        existingPost.setAuthor(createMember("testuser"));

        when(postRepository.findById(1))
                .thenReturn(Optional.of(existingPost));

        // when
        Post result = postService.findOne(1);

        //then
        assertNotNull(result);
        assertSame(existingPost, result);
        verify(postRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("findOne(): 존재하지 않는 게시글이면 DataNotFoundException이 발생한다")
    void shouldThrowDataNotFoundException_whenPostNotFoundOnFindOne() {
        // given
        when(postRepository.findById(1))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> postService.findOne(1));

        // then
        assertEquals("Post not found", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("create(): 제목과 내용이 유효하면 작성자와 함께 게시글을 저장한다")
    void shouldSavePostWithAuthor_whenValidInput() {
        // given
        Member member = createMember("testuser");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        // when
        postService.create("test title", "test content", member);

        // then
        verify(postRepository, times(1)).save(postCaptor.capture());

        Post savedPost = postCaptor.getValue();

        assertEquals("test title", savedPost.getTitle());
        assertEquals("test content", savedPost.getContent());
        assertEquals(member, savedPost.getAuthor());
    }

    @Test
    @DisplayName("create(): 제목이 null이면 InvalidInputException이 발생한다")
    void shouldThrowInvalidInputException_whenTitleIsNullOnCreate() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> postService.create(null, "test content", createMember("testuser")));

        // then
        assertEquals("제목은 필수항목입니다.", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("create(): 제목이 공백이면 InvalidInputException이 발생한다")
    void shouldThrowInvalidInputException_whenTitleIsBlankOnCreate() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> postService.create("", "test content", createMember("testuser")));

        // then
        assertEquals("제목은 필수항목입니다.", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("create(): 내용이 null이면 InvalidInputException이 발생한다")
    void shouldThrowInvalidInputException_whenContentIsNullOnCreate() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> postService.create("test title", null, createMember("testuser")));

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("create(): 내용이 공백이면 InvalidInputException이 발생한다")
    void shouldThrowInvalidInputException_whenContentIsBlankOnCreate() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> postService.create("test title", "", createMember("testuser")));

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("modify(): 작성자 본인이면 제목과 내용을 수정하고 저장한다")
    void shouldModifyPostTitleAndContent_whenAuthorMatches() {
        // given
        Post existingPost = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));
        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);

        // when
        postService.modify(1, "newTitle", "newContent", "testuser");

        // then
        verify(postRepository, times(1)).findById(1);
        verify(postRepository, times(1)).save(postCaptor.capture());

        Post result = postCaptor.getValue();

        assertEquals("newTitle", result.getTitle());
        assertEquals("newContent", result.getContent());
    }

    @Test
    @DisplayName("modify(): 게시글의 작성자가 null이면 IllegalStateException이 발생한다")
    void shouldThrowIllegalStateException_whenPostAuthorIsNullOnModify() {
        // given
        Post existingPost = createPost("testuser");
        existingPost.setAuthor(null);

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> postService.modify(1, "newTitle", "newContent", "testuser"));

        // then
        assertEquals("Post has no author", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("modify(): 수정자가 null이면 InvalidRequestException이 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsNullOnModify() {
        // given
        Post existingPost = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> postService.modify(1, "newTitle", "newContent", null));

        // then
        assertEquals("Username is null", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("modify(): 수정자가 작성자가 아니면 PostAccessDeniedException이 발생한다")
    void shouldThrowPostAccessDeniedException_whenModifyRequestedByNonAuthor() {
        // given
        Post existingPost = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> postService.modify(1, "newTitle", "newContent", "wronguser"));

        // then
        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("modify(): 제목이 null이면 InvalidInputException이 발생한다")
    void shouldThrowInvalidInputException_whenTitleIsNullOnModify() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> postService.modify(1, null, "newContent", "testuser"));

        // then
        assertEquals("제목은 필수항목입니다.", exception.getMessage());

        verify(postRepository, never()).findById(any());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("modify(): 제목이 공백이면 InvalidInputException이 발생한다")
    void shouldThrowInvalidInputException_whenTitleIsBlankOnModify() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> postService.modify(1, "", "newContent", "testuser"));

        // then
        assertEquals("제목은 필수항목입니다.", exception.getMessage());

        verify(postRepository, never()).findById(any());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("modify(): 내용이 null이면 InvalidInputException이 발생한다")
    void shouldThrowInvalidInputException_whenContentIsNullOnModify() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> postService.modify(1, "newTitle", null, "testuser"));

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());

        verify(postRepository, never()).findById(any());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("modify(): 내용이 공백이면 InvalidInputException이 발생한다")
    void shouldThrowInvalidInputException_whenContentIsBlankOnModify() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> postService.modify(1, "newTitle", "", "testuser"));

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());

        verify(postRepository, never()).findById(any());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("modify(): 존재하지 않는 게시글이면 DataNotFoundException이 발생한다")
    void shouldThrowDataNotFoundException_whenPostNotFoundOnModify() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> postService.modify(1, "testTitle", "testContent", "testuser"));

        // then
        assertEquals("Post not found", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("delete(): 작성자 본인이면 게시글을 삭제한다")
    void shouldDeletePost_whenAuthorMatches() {
        // given
        Post existingPost = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        postService.delete(1, "testuser");

        // then
        verify(postRepository, times(1)).findById(1);
        verify(postRepository, times(1)).delete(existingPost);
    }

    @Test
    @DisplayName("delete(): 존재하지 않는 게시글이면 DataNotFoundException이 발생한다")
    void shouldThrowDataNotFoundException_whenPostNotFoundOnDelete() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> postService.delete(1, "testuser"));

        // then
        assertEquals("Post not found", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("delete(): 게시글의 작성자가 null이면 IllegalStateException이 발생한다")
    void shouldThrowIllegalStateException_whenPostAuthorIsNullOnDelete() {
        // given
        Post existingPost = createPost("testuser");
        existingPost.setAuthor(null);

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> postService.delete(1, "testuser"));

        // then
        assertEquals("Post has no author", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("delete(): 수정자가 null이면 InvalidRequestException이 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsNullOnDelete() {
        // given
        Post existingPost = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> postService.delete(1, null));

        // then
        assertEquals("Username is null", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("delete(): 작성자가 아니면 PostAccessDeniedException이 발생한다")
    void shouldThrowPostAccessDeniedException_whenDeleteRequestedByNonAuthor() {
        // given
        Post existingPost = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> postService.delete(1, "wronguser"));

        // then
        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("getPostForModify(): 작성자 본인이면 게시글을 반환한다")
    void shouldReturnPost_whenGetPostForModifyRequestedByAuthor() {
        // given
        Post existingPost = new Post();
        existingPost.setId(1);
        existingPost.setTitle("test title");
        existingPost.setContent("test content");
        existingPost.setAuthor(createMember("testuser"));

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        Post result = postService.getPostForModify(1, "testuser");

        // then
        assertSame(existingPost, result);

        verify(postRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getPostForModify(): 게시글의 작성자가 null이면 IllegalStateException이 발생한다")
    void shouldThrowIllegalStateException_whenPostAuthorIsNullOnGetPostForModify() {
        // given
        Post existingPost = createPost("testuser");
        existingPost.setAuthor(null);

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> postService.getPostForModify(1, "wronguser"));

        // then
        assertEquals("Post has no author", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("getPostForModify(): 수정자가 null이면 InvalidRequestException이 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsNullOnGetPostForModify() {
        // given
        Post existingPost = createPost("testuser");

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> postService.getPostForModify(1, null));

        // then
        assertEquals("Username is null", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("getPostForModify(): 작성자가 아니면 PostAccessDeniedException이 발생한다")
    void shouldThrowPostAccessDeniedException_whenGetPostForModifyRequestedByNonAuthor() {
        // given
        Post existingPost = new Post();
        existingPost.setId(1);
        existingPost.setTitle("test title");
        existingPost.setContent("test content");
        existingPost.setAuthor(createMember("testuser"));

        when(postRepository.findById(1)).thenReturn(Optional.of(existingPost));

        // when
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> postService.getPostForModify(1, "wronguser"));

        // then
        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getPostForModify(): 존재하지 않는 게시글이면 DataNotFoundException이 발생한다")
    void shouldThrowDataNotFoundException_whenPostNotFoundOnGetPostForModify() {
        // given
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> postService.getPostForModify(1, "testuser"));

        // then
        assertEquals("Post not found", exception.getMessage());

        verify(postRepository, times(1)).findById(1);
    }
}
