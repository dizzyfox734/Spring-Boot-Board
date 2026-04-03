package dizzyfox734.springbootboard.post.domain;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.common.entity.BaseTimeEntity;
import dizzyfox734.springbootboard.member.domain.Member;
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
    private Member author;
}
