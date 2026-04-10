package dizzyfox734.springbootboard.comment.repository;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.repository.AuthorityRepository;
import dizzyfox734.springbootboard.post.domain.Post;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryJpaIntegrationTest {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Comment 삭제 시 실제 row는 남고 deleted_date가 채워진다")
    void shouldUpdateDeletedDateWithoutDeletingRow_whenDeletingComment() {
        Comment comment = createComment();
        Integer commentId = comment.getId();
        commentRepository.delete(comment);
        entityManager.flush();
        entityManager.clear();

        Number deletedRowCount = (Number) entityManager
                .createNativeQuery("select count(*) from comment where id = :id and deleted_date is not null")
                .setParameter("id", commentId)
                .getSingleResult();

        assertThat(deletedRowCount.longValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("soft delete 된 Comment는 ID 조회 결과가 비어 있어야 한다")
    void shouldReturnEmptyWhenFindingSoftDeletedCommentById() {
        Comment comment = createComment();
        commentRepository.delete(comment);
        entityManager.flush();
        entityManager.clear();

        Optional<Comment> result = commentRepository.findById(comment.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll 은 soft delete 되지 않은 Comment 만 조회한다")
    void shouldFindAllExcludingSoftDeletedComments() {
        Comment activeComment = createComment("testuser1", "active comment");
        Comment deletedComment = createComment("testuser2", "deleted comment");
        commentRepository.delete(deletedComment);
        entityManager.flush();
        entityManager.clear();

        assertThat(commentRepository.findAll())
                .extracting(Comment::getContent)
                .contains(activeComment.getContent())
                .doesNotContain(deletedComment.getContent());
    }

    private Comment createComment() {
        return createComment("testuser", "test comment");
    }

    private Comment createComment(String username, String content) {
        Authority roleUser = authorityRepository.findById("ROLE_USER")
                .orElseThrow(() -> new AssertionError("ROLE_USER 권한이 존재해야 합니다."));
        Member member = Member.create(
                username,
                "encodedPassword",
                "홍길동",
                username + "@example.com",
                Set.of(roleUser)
        );
        Post post = Post.create("test title", "test content", member);
        Comment comment = Comment.create(content, post, member);

        entityManager.persist(member);
        entityManager.persist(post);
        entityManager.persist(comment);
        entityManager.flush();
        entityManager.clear();

        return comment;
    }
}
