package dizzyfox734.springbootboard.comment.repository;

import dizzyfox734.springbootboard.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

}