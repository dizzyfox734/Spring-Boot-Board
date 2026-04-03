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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final PostService postService;
    private final MemberService memberService;
    private final CommentRepository commentRepository;

    public Comment create(Integer postId, String content, String username) {
        validatePostId(postId);
        validateUsername(username);
        validateCommentInput(content);

        Post post = this.postService.findOne(postId);
        Member author = this.memberService.getMember(username);

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(author);
        this.commentRepository.save(comment);

        return comment;
    }

    public Comment findOne(Integer id) {
        Optional<Comment> comment = this.commentRepository.findById(id);

        if (comment.isPresent()) {
            return comment.get();
        } else {
            throw new DataNotFoundException("comment not found");
        }
    }

    public Comment getCommentForModify(Integer commentId, String username) {
        validateCommentId(commentId);
        validateUsername(username);

        Comment comment = findOne(commentId);
        validateAuthor(comment, username);
        return comment;
    }

    public Comment modify(Integer commentId, String content, String username) {
        validateCommentId(commentId);
        validateUsername(username);
        validateCommentInput(content);

        Comment comment = findOne(commentId);
        validateAuthor(comment, username);

        comment.setContent(content);
        this.commentRepository.save(comment);
        return comment;
    }

    public Comment delete(Integer commentId, String username) {
        validateCommentId(commentId);
        validateUsername(username);

        Comment comment = findOne(commentId);
        validateAuthor(comment, username);

        this.commentRepository.delete(comment);
        return comment;
    }

    private void validateAuthor(Comment comment, String username) {
        if (comment.getAuthor() == null) {
            throw new IllegalStateException("Comment has no author");
        }

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("작성자만 접근할 수 있습니다.");
        }
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
        if (username == null) {
            throw new InvalidRequestException("Username is null");
        }
    }

    private void validateCommentInput(String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidInputException("내용은 필수항목입니다.");
        }
    }
}