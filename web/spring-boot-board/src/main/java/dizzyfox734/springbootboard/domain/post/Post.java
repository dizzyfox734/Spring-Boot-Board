package dizzyfox734.springbootboard.domain.post;

import dizzyfox734.springbootboard.domain.BaseTimeEntity;
import dizzyfox734.springbootboard.domain.comment.Comment;
import dizzyfox734.springbootboard.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Getter
@Setter
@Entity
@SQLDelete(sql = "UPDATE post SET deleted_date = NOW() WHERE id = ?")
@Where(clause = "deleted_date is null")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Comment> commentList;

    @ManyToOne
    private User author;
}
