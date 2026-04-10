package dizzyfox734.springbootboard.comment.service;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.comment.repository.CommentRepository;
import dizzyfox734.springbootboard.global.exception.AccessDeniedException;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.exception.InvalidRequestException;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.repository.MemberRepository;
import dizzyfox734.springbootboard.post.domain.Post;
import dizzyfox734.springbootboard.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Integer create(Integer postId, String content, String username) {
        validatePostId(postId);
        validateUsername(username);
        validateContent(content);

        Post post = getPost(postId);
        Member author = getMember(username);

        Comment comment = Comment.create(content, post, author);

        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public Comment getComment(Integer commentId) {
        validateCommentId(commentId);

        return commentRepository.findById(commentId)
                .orElseThrow(() -> new DataNotFoundException("comment not found"));
    }

    @Transactional(readOnly = true)
    public Comment getCommentForModify(Integer commentId, String username) {
        validateCommentId(commentId);
        validateUsername(username);

        Comment comment = getComment(commentId);
        validateAuthor(comment, username);
        return comment;
    }

    @Transactional
    public Integer modify(Integer commentId, String content, String username) {
        validateCommentId(commentId);
        validateUsername(username);

        Comment comment = getComment(commentId);
        validateAuthor(comment, username);

        comment.edit(content);
        commentRepository.save(comment);

        return comment.getPost().getId();
    }

    @Transactional
    public Integer delete(Integer commentId, String username) {
        validateCommentId(commentId);
        validateUsername(username);

        Comment comment = getComment(commentId);
        validateAuthor(comment, username);

        commentRepository.delete(comment);
        return comment.getPost().getId();
    }

    @Transactional(readOnly = true)
    public Integer getPostIdByCommentId(Integer commentId) {
        return getComment(commentId).getPost().getId();
    }

    private void validateAuthor(Comment comment, String username) {
        if (comment.getAuthor() == null) {
            throw new IllegalStateException("Comment has no author");
        }

        if (!comment.isWrittenBy(username)) {
            throw new AccessDeniedException("작성자만 접근할 수 있습니다.");
        }
    }

    private Post getPost(Integer postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new DataNotFoundException("Post not found"));
    }

    private Member getMember(String username) {
        return memberRepository.findOneWithAuthoritiesByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("user not found"));
    }

    private void validatePostId(Integer postId) {
        if (postId == null) {
            throw new InvalidRequestException("Post id is null");
        }
    }

    private void validateCommentId(Integer commentId) {
        if (commentId == null) {
            throw new InvalidRequestException("Comment id is null");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidRequestException("Username is null or blank");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수항목입니다.");
        }
    }
}
