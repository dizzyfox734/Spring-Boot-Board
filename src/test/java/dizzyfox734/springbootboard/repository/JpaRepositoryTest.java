package dizzyfox734.springbootboard.repository;

import dizzyfox734.springbootboard.domain.post.Post;
import dizzyfox734.springbootboard.domain.post.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class JpaRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @AfterEach
    public void cleanup() {
        postRepository.deleteAll();
    }

    @Test
    void jpa_테스트() {

        // given
        String title1 = "title1";
        String content1 = "content1";
        String title2 = "title2";
        String content2 = "content2";
        LocalDateTime now = LocalDateTime.of(2023, 5, 21, 0, 0, 0);

        Post q1 = new Post();
        q1.setTitle(title1);
        q1.setContent(content1);
        this.postRepository.save(q1);  // 첫번째 질문 저장

        Post q2 = new Post();
        q2.setTitle(title2);
        q2.setContent(content2);
        this.postRepository.save(q2);  // 두번째 질문 저장

        // when
        List<Post> all = this.postRepository.findAll();

        // then
        assertThat(all.size()).isEqualTo(2);
        Post post = all.get(0);

        System.out.println(">>>>>>>>>> createDate=" + post.getCreatedDate() + ", modifiedDate=" + post.getModifiedDate());

        assertThat(post.getTitle()).isEqualTo(title1);
        assertThat(post.getContent()).isEqualTo(content1);
        assertThat(post.getCreatedDate()).isAfter(now);
        assertThat(post.getModifiedDate()).isAfter(now);
    }

}
