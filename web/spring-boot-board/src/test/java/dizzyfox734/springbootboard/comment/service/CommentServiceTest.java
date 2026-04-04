package dizzyfox734.springbootboard.comment.service;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.comment.repository.CommentRepository;
import dizzyfox734.springbootboard.global.exception.AccessDeniedException;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.exception.InvalidInputException;
import dizzyfox734.springbootboard.global.exception.InvalidRequestException;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.service.MemberService;
import dizzyfox734.springbootboard.post.domain.Post;
import dizzyfox734.springbootboard.post.service.PostService;
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
public class CommentServiceTest {

    @Mock
    private PostService postService;

    @Mock
    private MemberService memberService;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    private Member createMember() {
        return Member.builder()
                .username("testuser")
                .build();
    }

    private Post createPost() {
        Post post = new Post();
        post.setId(1);
        return post;
    }

    private Comment createComment(Post post, Member member) {
        Comment comment = new Comment();
        comment.setId(1);
        comment.setContent("test content");
        comment.setPost(post);
        comment.setAuthor(member);
        return comment;
    }

    @Test
    @DisplayName("create(): 유효한 게시글, 내용, 작성자가 주어지면 댓글을 생성하고 저장한 뒤 댓글 ID를 반환한다")
    void shouldCreateAndSaveCommentAndReturnCommentId_whenValidPostContentAndAuthorAreGiven() {
        // given
        Member member = createMember();
        Post post = createPost();

        when(postService.getPost(1)).thenReturn(post);
        when(memberService.getMember("testuser")).thenReturn(member);
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment savedComment = invocation.getArgument(0);
                    savedComment.setId(1);
                    return savedComment;
                });

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        // when
        Integer result = commentService.create(1, "test content", "testuser");

        // then
        assertNotNull(result);
        assertEquals(1, result);

        verify(postService, times(1)).getPost(1);
        verify(memberService, times(1)).getMember("testuser");
        verify(commentRepository, times(1)).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertEquals("test content", savedComment.getContent());
        assertEquals(post, savedComment.getPost());
        assertEquals(member, savedComment.getAuthor());
    }

    @Test
    @DisplayName("create(): 게시글 ID가 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenPostIdIsNullForCreate() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.create(null, "test content", "testuser"));

        // then
        assertEquals("Post id is null", exception.getMessage());
        verifyNoInteractions(postService, memberService, commentRepository);
    }

    @Test
    @DisplayName("create(): 사용자명이 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsNullForCreate() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.create(1, "test content", null));

        // then
        assertEquals("Username is null or blank", exception.getMessage());
        verifyNoInteractions(postService, memberService, commentRepository);
    }

    @Test
    @DisplayName("create(): 사용자명이 공백이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsBlankForCreate() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.create(1, "test content", "   "));

        // then
        assertEquals("Username is null or blank", exception.getMessage());
        verifyNoInteractions(postService, memberService, commentRepository);
    }

    @Test
    @DisplayName("create(): 내용이 null이면 예외가 발생한다")
    void shouldThrowInvalidInputException_whenContentIsNullForCreate() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> commentService.create(1, null, "testuser"));

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());
        verifyNoInteractions(postService, memberService, commentRepository);
    }

    @Test
    @DisplayName("create(): 내용이 공백이면 예외가 발생한다")
    void shouldThrowInvalidInputException_whenContentIsBlankForCreate() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> commentService.create(1, "   ", "testuser"));

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());
        verifyNoInteractions(postService, memberService, commentRepository);
    }

    @Test
    @DisplayName("create(): 존재하지 않는 게시글 ID면 예외가 발생한다")
    void shouldPropagateException_whenPostDoesNotExistForCreate() {
        // given
        when(postService.getPost(1))
                .thenThrow(new DataNotFoundException("post not found"));

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> commentService.create(1, "test content", "testuser"));

        // then
        assertEquals("post not found", exception.getMessage());
        verify(postService, times(1)).getPost(1);
        verifyNoInteractions(memberService);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("create(): 존재하지 않는 회원이면 예외가 발생한다")
    void shouldPropagateException_whenMemberDoesNotExistForCreate() {
        // given
        Post post = createPost();

        when(postService.getPost(1)).thenReturn(post);
        when(memberService.getMember("testuser"))
                .thenThrow(new DataNotFoundException("member not found"));

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> commentService.create(1, "test content", "testuser"));

        // then
        assertEquals("member not found", exception.getMessage());
        verify(postService, times(1)).getPost(1);
        verify(memberService, times(1)).getMember("testuser");
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("getComment(): 존재하는 댓글 ID가 주어지면 댓글을 반환한다")
    void shouldReturnComment_whenCommentExists() {
        // given
        Member member = createMember();
        Post post = createPost();
        Comment comment = createComment(post, member);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        Comment result = commentService.getComment(1);

        // then
        assertNotNull(result);
        assertSame(comment, result);
        verify(commentRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getComment(): 존재하지 않는 댓글 ID가 주어지면 예외가 발생한다")
    void shouldThrowDataNotFoundException_whenCommentDoesNotExist() {
        // given
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> commentService.getComment(1));

        // then
        assertEquals("comment not found", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getCommentForModify(): 댓글 작성자와 요청 사용자가 같으면 댓글을 반환한다")
    void shouldReturnCommentForModify_whenRequesterIsAuthor() {
        // given
        Member member = createMember();
        Post post = createPost();
        Comment comment = createComment(post, member);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        Comment result = commentService.getCommentForModify(1, "testuser");

        // then
        assertNotNull(result);
        assertSame(comment, result);
        verify(commentRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getCommentForModify(): 댓글 ID가 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenCommentIdIsNullForGetCommentForModify() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.getCommentForModify(null, "testuser"));

        // then
        assertEquals("Comment id is null", exception.getMessage());
        verify(commentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getCommentForModify(): 요청 사용자명이 null이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsNullForGetCommentForModify() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.getCommentForModify(1, null));

        // then
        assertEquals("Username is null or blank", exception.getMessage());
        verify(commentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getCommentForModify(): 요청 사용자명이 공백이면 예외가 발생한다")
    void shouldThrowInvalidRequestException_whenUsernameIsBlankForGetCommentForModify() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.getCommentForModify(1, "   "));

        // then
        assertEquals("Username is null or blank", exception.getMessage());
        verify(commentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("getCommentForModify(): 존재하지 않는 댓글이면 예외가 발생한다")
    void shouldThrowDataNotFoundException_whenCommentDoesNotExistForGetCommentForModify() {
        // given
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> commentService.getCommentForModify(1, "testuser"));

        // then
        assertEquals("comment not found", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getCommentForModify(): 댓글 작성자가 없으면 예외가 발생한다")
    void shouldThrowIllegalStateException_whenCommentHasNoAuthorForGetCommentForModify() {
        // given
        Post post = createPost();
        Comment comment = createComment(post, null);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> commentService.getCommentForModify(1, "testuser"));

        // then
        assertEquals("Comment has no author", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("getCommentForModify(): 작성자가 아니면 접근 예외가 발생한다")
    void shouldThrowAccessDeniedException_whenRequesterIsNotAuthorForGetCommentForModify() {
        // given
        Member member = Member.builder()
                .username("wronguser")
                .build();
        Post post = createPost();
        Comment comment = createComment(post, member);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentService.getCommentForModify(1, "testuser"));

        // then
        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("modify(): 작성자 본인이 요청하면 댓글 내용을 수정하고 저장한 뒤 댓글 ID를 반환한다")
    void shouldModifyAndSaveCommentAndReturnCommentId_whenRequesterIsAuthor() {
        // given
        Member member = createMember();
        Post post = createPost();
        Comment comment = createComment(post, member);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        // when
        Integer result = commentService.modify(1, "new content", "testuser");

        // then
        assertNotNull(result);
        assertEquals(1, result);
        assertEquals("new content", comment.getContent());

        verify(commentRepository, times(1)).findById(1);
        verify(commentRepository, times(1)).save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertEquals("new content", savedComment.getContent());
    }

    @Test
    @DisplayName("modify(): 댓글 ID가 null이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowInvalidRequestExceptionAndNotSave_whenCommentIdIsNullForModify() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.modify(null, "new content", "testuser"));

        // then
        assertEquals("Comment id is null", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 요청 사용자명이 null이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowInvalidRequestExceptionAndNotSave_whenUsernameIsNullForModify() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.modify(1, "new content", null));

        // then
        assertEquals("Username is null or blank", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 요청 사용자명이 공백이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowInvalidRequestExceptionAndNotSave_whenUsernameIsBlankForModify() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.modify(1, "new content", "   "));

        // then
        assertEquals("Username is null or blank", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 수정 내용이 null이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowInvalidInputExceptionAndNotSave_whenContentIsNullForModify() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> commentService.modify(1, null, "testuser"));

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 수정 내용이 공백이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowInvalidInputExceptionAndNotSave_whenContentIsBlankForModify() {
        // when
        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> commentService.modify(1, "   ", "testuser"));

        // then
        assertEquals("내용은 필수항목입니다.", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 존재하지 않는 댓글이면 예외가 발생하고 저장하지 않는다")
    void shouldThrowDataNotFoundExceptionAndNotSave_whenCommentDoesNotExistForModify() {
        // given
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> commentService.modify(1, "new content", "testuser"));

        // then
        assertEquals("comment not found", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 댓글 작성자가 없으면 예외가 발생하고 저장하지 않는다")
    void shouldThrowIllegalStateExceptionAndNotSave_whenCommentHasNoAuthorForModify() {
        // given
        Post post = createPost();
        Comment comment = createComment(post, null);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> commentService.modify(1, "new content", "testuser"));

        // then
        assertEquals("Comment has no author", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("modify(): 작성자가 아니면 예외가 발생하고 저장하지 않는다")
    void shouldThrowAccessDeniedExceptionAndNotSave_whenRequesterIsNotAuthorForModify() {
        // given
        Member member = createMember();
        Post post = createPost();
        Comment comment = createComment(post, member);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentService.modify(1, "new content", "wronguser"));

        // then
        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 작성자 본인이 요청하면 댓글을 삭제하고 댓글 ID를 반환한다")
    void shouldDeleteCommentAndReturnCommentId_whenRequesterIsAuthor() {
        // given
        Member member = createMember();
        Post post = createPost();
        Comment comment = createComment(post, member);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        Integer result = commentService.delete(1, "testuser");

        // then
        assertNotNull(result);
        assertEquals(1, result);
        verify(commentRepository, times(1)).findById(1);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("delete(): 댓글 ID가 null이면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowInvalidRequestExceptionAndNotDelete_whenCommentIdIsNullForDelete() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.delete(null, "testuser"));

        // then
        assertEquals("Comment id is null", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 요청 사용자명이 null이면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowInvalidRequestExceptionAndNotDelete_whenUsernameIsNullForDelete() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.delete(1, null));

        // then
        assertEquals("Username is null or blank", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 요청 사용자명이 공백이면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowInvalidRequestExceptionAndNotDelete_whenUsernameIsBlankForDelete() {
        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class,
                () -> commentService.delete(1, "   "));

        // then
        assertEquals("Username is null or blank", exception.getMessage());
        verify(commentRepository, never()).findById(any());
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 존재하지 않는 댓글이면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowDataNotFoundExceptionAndNotDelete_whenCommentDoesNotExistForDelete() {
        // given
        when(commentRepository.findById(1)).thenReturn(Optional.empty());

        // when
        DataNotFoundException exception = assertThrows(DataNotFoundException.class,
                () -> commentService.delete(1, "testuser"));

        // then
        assertEquals("comment not found", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 댓글 작성자가 없으면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowIllegalStateExceptionAndNotDelete_whenCommentHasNoAuthorForDelete() {
        // given
        Post post = createPost();
        Comment comment = createComment(post, null);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> commentService.delete(1, "testuser"));

        // then
        assertEquals("Comment has no author", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    @DisplayName("delete(): 작성자가 아니면 예외가 발생하고 삭제하지 않는다")
    void shouldThrowAccessDeniedExceptionAndNotDelete_whenRequesterIsNotAuthorForDelete() {
        // given
        Member member = createMember();
        Post post = createPost();
        Comment comment = createComment(post, member);

        when(commentRepository.findById(1)).thenReturn(Optional.of(comment));

        // when
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> commentService.delete(1, "wronguser"));

        // then
        assertEquals("작성자만 접근할 수 있습니다.", exception.getMessage());
        verify(commentRepository, times(1)).findById(1);
        verify(commentRepository, never()).delete(any(Comment.class));
    }
}
