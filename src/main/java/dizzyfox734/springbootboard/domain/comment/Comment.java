package dizzyfox734.springbootboard.domain.comment;

import dizzyfox734.springbootboard.domain.BaseTimeEntity;
import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    private Post post;

    @ManyToOne
    private User author;
}