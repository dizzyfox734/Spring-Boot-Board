package dizzyfox734.springbootboard.service;

import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.user.User;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
import dizzyfox734.springbootboard.domain.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;

    public Page<Post> getList(int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("createdDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));

        return this.postRepository.findAll(pageable);
    }

    public Post findOne(Integer id) {
        Optional<Post> post = this.postRepository.findById(id);

        if (post.isPresent()) {
            return post.get();
        } else {
            throw new DataNotFoundException("Post not found");
        }
    }

    public void create(String title, String content, User user) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(user);
        this.postRepository.save(post);
    }

    public void modify(Post post, String title, String content) {
        post.setTitle(title);
        post.setContent(content);
        this.postRepository.save(post);
    }
}
