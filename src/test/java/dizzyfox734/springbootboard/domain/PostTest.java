package dizzyfox734.springbootboard.domain;

import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.post.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PostTest {

    @Autowired
    private PostRepository postRepository;

//    @AfterEach
//    public void cleanup() {
//        postRepository.deleteAll();
//    }

    @Test
    public void soft_삭제_테스트() {

        // when
        Post post  = new Post();
        post.setTitle("test");
        post.setContent("content");
        this.postRepository.save(post);

        assertThat(post.getId()).isNotNull();
        assertThat(post.getDeletedDate()).isNull();

        // given
        postRepository.delete(post);

        // then
        Optional<Post> deletedPost = postRepository.findById(post.getId());
        assertThat(deletedPost).isEmpty(); // where 테스트

//        // delete 테스트
//        assertThat(deletedPost).isNotEmpty();
//        assertThat(deletedPost).isNotNull();
    }

}
