package dizzyfox734.springbootboard.post.service;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.global.exception.AccessDeniedException;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.exception.InvalidRequestException;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.service.MemberService;
import dizzyfox734.springbootboard.post.domain.Post;
import dizzyfox734.springbootboard.post.repository.PostRepository;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberService memberService;

    @Transactional(readOnly = true)
    public Page<Post> findPosts(int page, String kw) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createdDate"));

        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        if (kw == null || kw.isBlank()) {
            return postRepository.findAll(pageable);
        }

        Specification<Post> spec = search(kw);

        return postRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Post getPost(Integer postId) {
        validatePostId(postId);

        return postRepository.findById(postId)
                .orElseThrow(() -> new DataNotFoundException("Post not found"));
    }

    /**
     * 포스트 생성
     * @param title
     * @param content
     * @param member
     * @return 생성한 포스트 id
     */
    @Transactional
    public Integer create(String title, String content, String username) {
        validateUsername(username);
        Member member = memberService.getMember(username);
        Post post = Post.create(title, content, member);

        return postRepository.save(post).getId();
    }

    /**
     * 포스트 수정
     * @param postId
     * @param title
     * @param content
     * @param username
     * @return 수정한 포스트 id
     */
    @Transactional
    public Integer modify(Integer postId, String title, String content, String username) {
        validatePostId(postId);

        Post post = getPost(postId);
        validateAuthor(post, username);

        post.edit(title, content);

        return postRepository.save(post).getId();
    }

    /**
     * 포스트 삭제
     * @param postId
     * @param username
     * @return 삭제한 포스트 id
     */
    @Transactional
    public Integer delete(Integer postId, String username) {
        validatePostId(postId);

        Post post = getPost(postId);
        validateAuthor(post, username);

        postRepository.delete(post);
        return post.getId();
    }

    /**
     * 본인이 작성한 포스트인지 확인 후 포스트 반환
     * @param postId 수정하려는 post의 id
     * @param username 수정을 요청하는 사용자의 username
     * @return post 수정하려는 포스트 반환
     */
    @Transactional(readOnly = true)
    public Post getPostForModify(Integer postId, String username) {
        validatePostId(postId);

        Post post = getPost(postId);
        validateAuthor(post, username);
        return post;
    }

    /**
     * 검색
     * @param kw
     * @return
     */
    private Specification<Post> search(String kw) {
        return new Specification<>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Predicate toPredicate(Root<Post> post,
                                         CriteriaQuery<?> query,
                                         CriteriaBuilder cb) {
                query.distinct(true);

                Join<Post, Member> author = post.join("author", JoinType.LEFT);
                Join<Post, Comment> comment = post.join("commentList", JoinType.LEFT);
                Join<Comment, Member> commentAuthor = comment.join("author", JoinType.LEFT);

                return cb.or(
                        cb.like(post.get("title"), "%" + kw + "%"),
                        cb.like(post.get("content"), "%" + kw + "%"),
                        cb.like(author.get("username"), "%" + kw + "%"),
                        cb.like(commentAuthor.get("username"), "%" + kw + "%")
                );
            }
        };
    }

    private void validatePostId(Integer postId) {
        if (postId == null) {
            throw new InvalidRequestException("Post id is null");
        }
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidRequestException("Username is null or blank");
        }
    }

    private void validateAuthor(Post post, String username) {
        if (post.getAuthor() == null) {
            throw new IllegalStateException("Post has no author");
        }

        validateUsername(username);

        if (!post.isWrittenBy(username)) {
            throw new AccessDeniedException("작성자만 접근할 수 있습니다.");
        }
    }
}
