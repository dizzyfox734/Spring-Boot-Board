package dizzyfox734.springbootboard;

import dizzyfox734.springbootboard.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringBootBoardApplicationTests {

	@Autowired
	private PostService postService;

	@Test
	void 대량의_테스트_데이터_만들기() {
		for (int i = 1; i <= 300; i++) {
			String title = String.format("테스트 데이터:[%03d]", i);
			String content = "No content";
			this.postService.create(title, content, null);
		}
	}
}
