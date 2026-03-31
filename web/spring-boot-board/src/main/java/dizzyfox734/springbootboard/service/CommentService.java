package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.domain.comment.Comment;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.comment.CommentRepository;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
import dizzyfox734.springbootboard.exception.InvalidRequestException;
import dizzyfox734.springbootboard.exception.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final PostService postService;
    private final MemberService memberService;
    private final CommentRepository commentRepository;

    public Comment create(Post post, String content, Member author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(author);
        this.commentRepository.save(comment);

        return comment;
    }

    public Comment create(Integer postId, String content, String username) {
        if (postId == null) {
            throw new InvalidRequestException("Post id is null");
        }

        if (username == null) {
            throw new InvalidRequestException("Username is null");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new InvalidRequestException("Comment content is blank");
        }

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
        Comment comment = findOne(commentId);
        validateAuthor(comment, username);
        return comment;
    }

    public Comment modify(Integer commentId, String content, String username) {
        Comment comment = findOne(commentId);
        validateAuthor(comment, username);

        comment.setContent(content);
        this.commentRepository.save(comment);
        return comment;
    }

    public Comment delete(Integer commentId, String username) {
        Comment comment = findOne(commentId);
        validateAuthor(comment, username);

        this.commentRepository.delete(comment);
        return comment;
    }

    private void validateAuthor(Comment comment, String username) {
        if (comment.getAuthor() == null) {
            throw new IllegalStateException("Comment has no author");
        }

        if (username == null) {
            throw new InvalidRequestException("Username is null");
        }

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("작성자만 접근할 수 있습니다.");
        }
    }
}