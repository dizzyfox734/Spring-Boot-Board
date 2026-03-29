package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.domain.comment.Comment;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
import dizzyfox734.springbootboard.domain.post.PostRepository;
import dizzyfox734.springbootboard.exception.InvalidPostInputException;
import dizzyfox734.springbootboard.exception.PostAccessDeniedException;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public Page<Post> getList(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        Specification<Post> spec = search(kw);

        return this.postRepository.findAll(spec, pageable);
    }

    public Post findOne(Integer id) {
        Optional<Post> post = this.postRepository.findById(id);

        if (post.isPresent()) {
            return post.get();
        } else {
            throw new DataNotFoundException("Post not found");
        }
    }

    public void create(String title, String content, Member member) {
        validatePostInput(title, content);

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(member);
        this.postRepository.save(post);
    }

    public void modify(Integer postId, String title, String content, String username) {
        validatePostInput(title, content);
        Post post = findOne(postId);
        validateAuthor(post, username);

        post.setTitle(title);
        post.setContent(content);
        this.postRepository.save(post);
    }

    public void delete(Integer postId, String username) {
        Post post = findOne(postId);
        validateAuthor(post, username);

        this.postRepository.delete(post);
    }

    /**
     * 본인이 작성한 포스트인지 확인 후 포스트 반환
     * @param postId 수정하려는 post의 id
     * @param username 수정을 요청하는 사용자의 username
     * @return post 수정하려는 포스트 반환
     */
    public Post getPostForModify(Integer postId, String username) {
        Post post = findOne(postId);
        validateAuthor(post, username);
        return post;
    }

    /**
     * 검색
     */
    private Specification<Post> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<Post> p, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);  // 중복 제거
                Join<Post, Member> u1 = p.join("author", JoinType.LEFT);
                Join<Post, Comment> c = p.join("commentList", JoinType.LEFT);
                Join<Comment, Member> u2 = c.join("author", JoinType.LEFT);

                return cb.or(cb.like(p.get("title"), "%" + kw + "%"),
                        cb.like(p.get("content"), "%" + kw + "%"),
                        cb.like(u1.get("username"), "%" + kw + "%"),
                        cb.like(p.get("content"), "%" + kw + "%"),
                        cb.like(u2.get("username"), "%" + kw + "%"));
            }
        };
    }

    private void validateAuthor(Post post, String username) {
        if (post.getAuthor() == null || !post.getAuthor().getUsername().equals(username)) {
            throw new PostAccessDeniedException("작성자만 접근할 수 있습니다.");
        }
    }

    private void validatePostInput(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new InvalidPostInputException("제목은 필수항목입니다.");
        }
        if (content == null || content.isBlank()) {
            throw new InvalidPostInputException("내용은 필수항목입니다.");
        }
    }
}
