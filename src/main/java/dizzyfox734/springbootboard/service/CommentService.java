package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.domain.comment.Comment;
import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.comment.CommentRepository;
import dizzyfox734.springbootboard.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CommentService {

    private final CommentRepository commentRepository;


    public void create(Post post, String content, User author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setAuthor(author);
        this.commentRepository.save(comment);
    }
}