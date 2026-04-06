package dizzyfox734.springbootboard.comment.domain;

import dizzyfox734.springbootboard.common.entity.BaseTimeEntity;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE comment SET deleted_date = NOW() WHERE id = ?")
@Where(clause = "deleted_date is null")
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    private Post post;

    @ManyToOne
    private Member author;

    private Comment(String content, Post post, Member author) {
        validateContent(content);
        validatePost(post);
        validateAuthor(author);

        this.content = content;
        this.post = post;
        this.author = author;
    }

    public static Comment create(String content, Post post, Member author) {
        return new Comment(content, post, author);
    }

    public void edit(String content) {
        validateContent(content);
        this.content = content;
    }

    public boolean isWrittenBy(String username) {
        return author != null
                && author.getUsername() != null
                && author.getUsername().equals(username);
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("내용은 필수항목입니다.");
        }
    }

    private void validatePost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("게시글 정보는 필수입니다.");
        }
    }

    private void validateAuthor(Member author) {
        if (author == null) {
            throw new IllegalArgumentException("작성자는 필수입니다.");
        }
    }
}
