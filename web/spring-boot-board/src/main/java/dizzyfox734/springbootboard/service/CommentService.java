package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.domain.comment.Comment;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.comment.CommentRepository;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;


    public Comment create(Post post, String content, Member author) {
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

    public void modify(Comment comment, String content) {
        comment.setContent(content);
        this.commentRepository.save(comment);
    }

    public void delete(Comment comment) {
        this.commentRepository.delete(comment);
    }
}