package dizzyfox734.springbootboard.post.repository;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.comment.repository.CommentRepository;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.repository.AuthorityRepository;
import dizzyfox734.springbootboard.post.domain.Post;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryJpaIntegrationTest {

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Post soft delete 후 기본 조회에서는 제외된다")
    void shouldExcludeSoftDeletedPostFromDefaultQueries() {
        Post activePost = createPost("active post", "active content", "activeuser");
        Post deletedPost = createPost("deleted post", "deleted content", "deleteduser");
        postRepository.delete(deletedPost);
        entityManager.flush();
        entityManager.clear();

        assertThat(postRepository.findAll())
                .extracting(Post::getTitle)
                .contains(activePost.getTitle())
                .doesNotContain(deletedPost.getTitle());
    }

    @Test
    @DisplayName("soft delete 된 Post는 ID 조회 결과가 비어 있어야 한다")
    void shouldReturnEmptyWhenFindingSoftDeletedPostById() {
        Post post = createPost();
        postRepository.delete(post);
        entityManager.flush();
        entityManager.clear();

        Optional<Post> result = postRepository.findById(post.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Post 삭제 시 연관 Comment가 의도대로 cascade soft delete 된다")
    void shouldCascadeSoftDeleteCommentsWhenDeletingPost() {
        Post post = createPost();
        Integer postId = post.getId();
        Comment comment = createComment(post, "comment author", "cascade deleted comment");
        Integer commentId = comment.getId();

        Post savedPost = postRepository.findById(postId).orElseThrow();
        postRepository.delete(savedPost);
        entityManager.flush();
        entityManager.clear();

        Number softDeletedPostCount = (Number) entityManager
                .createNativeQuery("select count(*) from post where id = :id and deleted_date is not null")
                .setParameter("id", postId)
                .getSingleResult();
        Number softDeletedCommentCount = (Number) entityManager
                .createNativeQuery("select count(*) from comment where id = :id and deleted_date is not null")
                .setParameter("id", commentId)
                .getSingleResult();

        assertThat(softDeletedPostCount.longValue()).isEqualTo(1L);
        assertThat(softDeletedCommentCount.longValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("soft delete 된 Comment는 Post 연관 컬렉션 조회에서 제외된다")
    void shouldExcludeSoftDeletedCommentsFromPostCommentList() {
        Post post = createPost();
        Integer postId = post.getId();
        Comment activeComment = createComment(post, "active comment author", "active comment");
        Comment deletedComment = createComment(post, "deleted comment author", "deleted comment");
        commentRepository.delete(deletedComment);
        entityManager.flush();
        entityManager.clear();

        Post result = postRepository.findById(postId).orElseThrow();

        assertThat(result.getCommentList())
                .extracting(Comment::getContent)
                .contains(activeComment.getContent())
                .doesNotContain(deletedComment.getContent());
    }

    @Test
    @DisplayName("findAll(Pageable) 은 soft delete 되지 않은 Post 만 페이징 조회한다")
    void shouldFindPagedPostsExcludingSoftDeletedPosts() {
        Post activePost = createPost("page active post", "page active content", "pageactiveuser");
        Post deletedPost = createPost("page deleted post", "page deleted content", "pagedeleteduser");
        postRepository.delete(deletedPost);
        entityManager.flush();
        entityManager.clear();

        Page<Post> result = postRepository.findAll(PageRequest.of(0, 20));

        assertThat(result.getContent())
                .extracting(Post::getTitle)
                .contains(activePost.getTitle())
                .doesNotContain(deletedPost.getTitle());
    }

    @Test
    @DisplayName("findAll(Specification, Pageable) 은 조건에 맞는 soft delete 되지 않은 Post 만 조회한다")
    void shouldFindPostsBySpecificationAndPageableExcludingSoftDeletedPosts() {
        Post activePost = createPost("spec active post", "spec active content", "specactiveuser");
        Post deletedPost = createPost("spec deleted post", "spec deleted content", "specdeleteduser");
        postRepository.delete(deletedPost);
        entityManager.flush();
        entityManager.clear();

        Specification<Post> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("title"), "spec%");
        Page<Post> result = postRepository.findAll(spec, PageRequest.of(0, 20));

        assertThat(result.getContent())
                .extracting(Post::getTitle)
                .contains(activePost.getTitle())
                .doesNotContain(deletedPost.getTitle());
    }

    private Post createPost() {
        return createPost("test title", "test content", "testuser");
    }

    private Post createPost(String title, String content, String username) {
        Member member = createMember(username);
        Post post = Post.create(title, content, member);

        entityManager.persist(post);
        entityManager.flush();
        entityManager.clear();

        return post;
    }

    private Comment createComment(Post post, String username, String content) {
        Member member = createMember(username);
        Post managedPost = entityManager.find(Post.class, post.getId());
        Comment comment = Comment.create(content, managedPost, member);

        entityManager.persist(comment);
        entityManager.flush();
        entityManager.clear();

        return comment;
    }

    private Member createMember(String username) {
        Authority roleUser = authorityRepository.findById("ROLE_USER")
                .orElseThrow(() -> new AssertionError("ROLE_USER 권한이 존재해야 합니다."));
        Member member = Member.create(
                username,
                "encodedPassword",
                "홍길동",
                username + "@example.com",
                Set.of(roleUser)
        );

        entityManager.persist(member);
        return member;
    }
}
