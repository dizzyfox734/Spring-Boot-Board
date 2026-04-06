package dizzyfox734.springbootboard.post.domain;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.common.entity.BaseTimeEntity;
import dizzyfox734.springbootboard.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private Post(String title, String content, Member author) {
        validateTitle(title);
        validateContent(content);
        validateAuthor(author);

        this.title = title;
        this.content = content;
        this.author = author;
    }

    public static Post create(String title, String content, Member author) {
        return new Post(title, content, author);
    }

    public void edit(String title, String content) {
        validateTitle(title);
        validateContent(content);

        this.title = title;
        this.content = content;
    }

    public boolean isWrittenBy(String username) {
        return author != null
                && author.getUsername() != null
                && author.getUsername().equals(username);
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목은 필수항목입니다.");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수항목입니다.");
        }
    }

    private void validateAuthor(Member author) {
        if (author == null) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }
    }
}
