package dizzyfox734.springbootboard.comment.service;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.comment.repository.CommentRepository;
import dizzyfox734.springbootboard.global.exception.AccessDeniedException;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.exception.InvalidRequestException;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.repository.MemberRepository;
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
class CommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

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
        return Post.create("post title", "post content", createMember(username));
    }

    private Comment createComment(Post post, Member member) {
        return Comment.create("test content", post, member);
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

    private void setCommentId(Comment comment, Integer id) {
        try {
            Field field = Comment.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(comment, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("create(): 유효한 게시글, 내용, 작성자가 주어지면 댓글을 생성하고 저장한 뒤 댓글 ID를 반환한다")
    void shouldCreateAndSaveCommentAndReturnCommentId_whenValidArgumentsAreGiven() {
        // given
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        setPostId(post, 1);

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(memberRepository.findOneWithAuthoritiesByUsername("testuser")).thenReturn(Optional.of(member));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment savedComment = invocation.getArgument(0);
                    setCommentId(savedComment, 1);
                    return savedComment;
                });

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        // when
        Integer result = commentService.create(1, "test content", "testuser");

        // then
        assertEquals(1, result);
        verify(postRepository).findById(1);
        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertEquals("test content", savedComment.getContent());
        assertEquals(post, savedComment.getPost());
        assertEquals(member, savedComment.getAuthor());
    }

    @Test
    @DisplayName("create(): 게시글 ID가 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenPostIdIsNullForCreate() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> commentService.create(null, "test content", "testuser")
        );

        assertEquals("Post id is null", exception.getMessage());
        verifyNoInteractions(postRepository, memberRepository, commentRepository);
    }

    @Test
    @DisplayName("create(): 사용자명이 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsNullForCreate() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> commentService.create(1, "test content", null)
        );

        assertEquals("Username is null or blank", exception.getMessage());
        verifyNoInteractions(postRepository, memberRepository, commentRepository);
    }

    @Test
    @DisplayName("create(): 내용이 null이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenContentIsNullForCreate() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.create(1, null, "testuser")
        );

        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): 내용이 공백이면 예외가 발생한다")
    void shouldThrowIllegalArgumentException_whenContentIsBlankForCreate() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.create(1, "   ", "testuser")
        );

        assertEquals("내용은 필수항목입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("create(): 존재하지 않는 게시글이면 예외가 발생한다")
    void shouldPropagateException_whenPostDoesNotExistForCreate() {
        when(postRepository.findById(1)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> commentService.create(1, "test content", "testuser")
        );

        assertEquals("Post not found", exception.getMessage());
        verify(postRepository).findById(1);
        verifyNoInteractions(memberRepository);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("create(): 존재하지 않는 회원이면 예외가 발생한다")
    void shouldPropagateException_whenMemberDoesNotExistForCreate() {
        Post post = createPost("postwriter");
        setPostId(post, 1);

        when(postRepository.findById(1)).thenReturn(Optional.of(post));
        when(memberRepository.findOneWithAuthoritiesByUsername("testuser"))
                .thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> commentService.create(1, "test content", "testuser")
        );

        assertEquals("user not found", exception.getMessage());
        verify(postRepository).findById(1);
        verify(memberRepository).findOneWithAuthoritiesByUsername("testuser");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("getComment(): 존재하는 댓글 ID가 주어지면 댓글을 반환한다")
    void shouldReturnComment_whenCommentExists() {
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        Comment result = commentService.getComment(1);

        assertSame(comment, result);
        verify(commentRepository).findById(1);
    }

    @Test
    @DisplayName("getComment(): 존재하지 않는 댓글 ID가 주어지면 예외가 발생한다")
    void shouldThrowDataNotFoundException_whenCommentDoesNotExist() {
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> commentService.getComment(1)
        );

        assertEquals("comment not found", exception.getMessage());
        verify(commentRepository).findById(1);
    }

    @Test
    @DisplayName("getCommentForModify(): 댓글 작성자와 요청 사용자가 같으면 댓글을 반환한다")
    void shouldReturnCommentForModify_whenRequesterIsAuthor() {
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        Comment result = commentService.getCommentForModify(1, "testuser");

        assertSame(comment, result);
        verify(commentRepository).findById(1);
    }

    @Test
    @DisplayName("getCommentForModify(): 댓글 ID가 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenCommentIdIsNullForGetCommentForModify() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> commentService.getCommentForModify(null, "testuser")
        );

        assertEquals("Comment id is null", exception.getMessage());
        verify(commentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getCommentForModify(): 요청 사용자명이 공백이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsBlankForGetCommentForModify() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> commentService.getCommentForModify(1, "   ")
        );

        assertEquals("Username is null or blank", exception.getMessage());
        verify(commentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getCommentForModify(): 존재하지 않는 댓글이면 예외가 발생한다")
    void shouldThrowDataNotFoundException_whenCommentDoesNotExistForGetCommentForModify() {
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> commentService.getCommentForModify(1, "testuser")
        );

        assertEquals("comment not found", exception.getMessage());
        verify(commentRepository).findById(1);
    }

    @Test
    @DisplayName("getCommentForModify(): 작성자가 아니면 접근 예외가 발생한다")
    void shouldThrowAccessDeniedException_whenRequesterIsNotAuthorForGetCommentForModify() {
        Member member = createMember("wronguser");
        Post post = createPost("postwriter");
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> commentService.getCommentForModify(1, "testuser")
        );

        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());
        verify(commentRepository).findById(1);
    }

    @Test
    @DisplayName("modify(): 작성자 본인이 요청하면 댓글 내용을 수정하고 저장한 뒤 게시글 ID를 반환한다")
    void shouldModifyAndSaveCommentAndReturnPostId_whenRequesterIsAuthor() {
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        setPostId(post, 100);
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        Integer result = commentService.modify(1, "new content", "testuser");

        assertEquals(100, result);
        assertEquals("new content", comment.getContent());

        verify(commentRepository).findById(1);
        verify(commentRepository).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertEquals("new content", savedComment.getContent());
    }

    @Test
    @DisplayName("modify(): 댓글 ID가 null이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowInvalidRequestExceptionAndNotSave_whenCommentIdIsNullForModify() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> commentService.modify(null, "new content", "testuser")
        );

        assertEquals("Comment id is null", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 수정 내용이 null이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowIllegalArgumentExceptionAndNotSave_whenContentIsNullForModify() {
        // given
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.modify(1, null, "testuser")
        );

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());
        verify(commentRepository).findById(1);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 존재하지 않는 댓글이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowDataNotFoundExceptionAndNotSave_whenCommentDoesNotExistForModify() {
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> commentService.modify(1, "new content", "testuser")
        );

        assertEquals("comment not found", exception.getMessage());
        verify(commentRepository).findById(1);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 작성자가 아니면 예외가 발생하고 저장하지 않는다")
    void shouldThrowAccessDeniedExceptionAndNotSave_whenRequesterIsNotAuthorForModify() {
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> commentService.modify(1, "new content", "wronguser")
        );

        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());
        verify(commentRepository).findById(1);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 작성자 본인이 요청하면 댓글을 삭제하고 게시글 ID를 반환한다")
    void shouldDeleteCommentAndReturnPostId_whenRequesterIsAuthor() {
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        setPostId(post, 1);
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        Integer result = commentService.delete(1, "testuser");

        assertEquals(1, result);
        verify(commentRepository).findById(1);
        verify(commentRepository).delete(comment);
    }

    @Test
    @DisplayName("delete(): 댓글 ID가 null이면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowInvalidRequestExceptionAndNotDelete_whenCommentIdIsNullForDelete() {
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> commentService.delete(null, "testuser")
        );

        assertEquals("Comment id is null", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 존재하지 않는 댓글이면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowDataNotFoundExceptionAndNotDelete_whenCommentDoesNotExistForDelete() {
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        DataNotFoundException exception = assertThrows(
                DataNotFoundException.class,
                () -> commentService.delete(1, "testuser")
        );

        assertEquals("comment not found", exception.getMessage());
        verify(commentRepository).findById(1);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 작성자가 아니면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowAccessDeniedExceptionAndNotDelete_whenRequesterIsNotAuthorForDelete() {
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> commentService.delete(1, "wronguser")
        );

        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());
        verify(commentRepository).findById(1);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("getPostIdByCommentId(): 댓글 ID로 게시글 ID를 반환한다")
    void shouldReturnPostId_whenCommentExistsForGetPostIdByCommentId() {
        Member member = createMember("testuser");
        Post post = createPost("postwriter");
        setPostId(post, 100);
        Comment comment = createComment(post, member);
        setCommentId(comment, 1);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        Integer result = commentService.getPostIdByCommentId(1);

        assertEquals(100, result);
        verify(commentRepository).findById(1);
    }
}
