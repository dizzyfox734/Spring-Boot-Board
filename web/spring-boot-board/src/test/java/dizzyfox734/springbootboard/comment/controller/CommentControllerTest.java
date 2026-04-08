package dizzyfox734.springbootboard.comment.controller;

import dizzyfox734.springbootboard.comment.domain.Comment;
import dizzyfox734.springbootboard.comment.service.CommentService;
import dizzyfox734.springbootboard.global.exception.AccessDeniedException;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.utils.MarkdownUtil;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.post.domain.Post;
import dizzyfox734.springbootboard.post.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@DisplayName("CommentController")
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @MockBean
    private CommentService commentService;

    @MockBean(name = "markdownUtil")
    private MarkdownUtil markdownUtil;

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/post/list",
                                    "/post/detail/**"
                            ).permitAll()

                            .requestMatchers(
                                    "/comment/create/**",
                                    "/comment/modify/**",
                                    "/comment/delete/**"
                            ).authenticated()

                            // 그 외 요청은 일단 허용하지 않음
                            .anyRequest().denyAll()
                    )
                    .formLogin(form -> form.disable());

            return http.build();
        }
    }

    @Nested
    @DisplayName("POST /comment/create/{postId}")
    class CreatePostTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("유효한 입력이면 댓글을 생성하고 게시글 상세로 리다이렉트한다")
        void shouldCreateCommentAndRedirectToPostDetail_whenValidInputIsGiven() throws Exception {
            given(commentService.create(1, "test comment", "testuser"))
                    .willReturn(2);

            mockMvc.perform(post("/comment/create/1")
                            .with(csrf())
                            .param("content", "test comment"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/post/detail/1#comment_2"));

            then(postService).shouldHaveNoInteractions();
            then(commentService).should().create(1, "test comment", "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("입력값 검증에 실패하면 게시글 상세 페이지를 다시 보여준다")
        void shouldReturnPostDetailPage_whenValidationFailsOnCreate() throws Exception {
            Authority authority = Authority.builder()
                    .name("ROLE_USER")
                    .build();
            Member member = Member.create(
                    "testuser",
                    "encodedPassword",
                    "홍길동",
                    "test@example.com",
                    Set.of(authority)
            );
            Post post = Post.create("test title", "test content", member);

            given(postService.getPost(1)).willReturn(post);
            given(markdownUtil.markdown(anyString())).willReturn("<p>test content</p>"); // Thymeleaf 렌더링 깨짐 방지

            mockMvc.perform(post("/comment/create/1")
                            .with(csrf())
                            .param("content", ""))
                    .andExpect(status().isOk())
                    .andExpect(view().name("post/detail"))
                    .andExpect(model().attributeExists("post"));

            then(postService).should().getPost(1);
            then(commentService).should(never()).create(anyInt(), anyString(), anyString());
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("존재하지 않는 게시글이면 예외가 발생한다")
        void shouldThrowException_whenPostDoesNotExistOnCreate() throws Exception {
            given(postService.getPost(1)).willThrow(new DataNotFoundException("Post not found"));

            mockMvc.perform(post("/comment/create/1")
                            .with(csrf())
                            .param("content", ""))
                    .andExpect(status().isNotFound())
                    .andExpect(view().name("error/404"))
                    .andExpect(model().attribute("errorMessage", "Post not found"));

            then(postService).should().getPost(1);
            then(commentService).should(never()).create(anyInt(), anyString(), anyString());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 댓글 생성 요청에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAttemptsToCreateComment() throws Exception {
            mockMvc.perform(post("/comment/create/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /comment/modify/{commentId}")
    class ModifyGetTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자는 댓글 수정 페이지를 조회할 수 있다")
        void shouldReturnModifyPage_whenRequesterIsAuthor() throws Exception {
            Comment comment = mock(Comment.class);
            given(comment.getContent()).willReturn("test comment");
            given(commentService.getCommentForModify(1, "testuser")).willReturn(comment);

            mockMvc.perform(get("/comment/modify/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("comment/form"))
                    .andExpect(model().attribute("commentDto", hasProperty("content", is("test comment"))));

            then(commentService).should().getCommentForModify(1, "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자가 아니면 접근 예외가 발생한다")
        void shouldThrowAccessDeniedException_whenRequesterIsNotAuthor() throws Exception {
            given(commentService.getCommentForModify(1, "testuser"))
                    .willThrow(new AccessDeniedException("작성자만 접근할 수 있습니다."));

            mockMvc.perform(get("/comment/modify/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error/403"))
                    .andExpect(model().attribute("errorMessage", "작성자만 접근할 수 있습니다."));

            then(commentService).should().getCommentForModify(1, "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("존재하지 않는 댓글이면 예외가 발생한다")
        void shouldThrowException_whenCommentDoesNotExist() throws Exception {
            given(commentService.getCommentForModify(1, "testuser")).willThrow(new DataNotFoundException("comment not found"));

            mockMvc.perform(get("/comment/modify/1")
                            .with(csrf())
                            .param("content", ""))
                    .andExpect(status().isNotFound())
                    .andExpect(view().name("error/404"))
                    .andExpect(model().attribute("errorMessage", "comment not found"));

            then(commentService).should().getCommentForModify(1, "testuser");
            then(commentService).should(never()).create(anyInt(), anyString(), anyString());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 댓글 수정 페이지에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAccessesModifyPage() throws Exception {
            mockMvc.perform(get("/comment/modify/1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /comment/modify/{commentId}")
    class ModifyPostTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("유효한 입력이면 댓글을 수정하고 게시글 상세로 리다이렉트한다")
        void shouldModifyCommentAndRedirectToPostDetail_whenValidInputIsGiven() throws Exception {
            given(commentService.modify(1, "test comment", "testuser"))
                    .willReturn(3);

            mockMvc.perform(post("/comment/modify/1")
                            .with(csrf())
                            .param("content", "test comment"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/post/detail/3#comment_1"));

            then(commentService).should().modify(1, "test comment", "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("입력값 검증에 실패하면 댓글 수정 페이지를 다시 보여준다")
        void shouldReturnModifyPage_whenValidationFails() throws Exception {
            mockMvc.perform(post("/comment/modify/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("comment/form"))
                    .andExpect(model().hasErrors());

            then(commentService).should(never()).modify(anyInt(), anyString(), anyString());
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자가 아니면 수정 요청 시 예외가 발생한다")
        void shouldThrowAccessDeniedException_whenRequesterIsNotAuthorForModify() throws Exception {
            given(commentService.modify(1, "test comment", "testuser"))
                    .willThrow(new AccessDeniedException("작성자만 접근할 수 있습니다."));

            mockMvc.perform(post("/comment/modify/1")
                            .with(csrf())
                            .param("content", "test comment"))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error/403"))
                    .andExpect(model().attribute("errorMessage", "작성자만 접근할 수 있습니다."));

            then(commentService).should().modify(1, "test comment", "testuser");
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 댓글 수정 요청에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAttemptsToModifyComment() throws Exception {
            mockMvc.perform(post("/comment/modify/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /comment/delete/{commentId}")
    class DeletePostTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자는 댓글을 삭제하고 게시글 상세로 리다이렉트한다")
        void shouldDeleteCommentAndRedirectToPostDetail_whenRequesterIsAuthor()  throws Exception {
            given(commentService.getPostIdByCommentId(1)).willReturn(3);

            mockMvc.perform(post("/comment/delete/1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/post/detail/3"));

            then(commentService).should().getPostIdByCommentId(1);
            then(commentService).should().delete(1, "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자가 아니면 삭제 요청 시 예외가 발생한다")
        void shouldThrowAccessDeniedException_whenRequesterIsNotAuthorForDelete() throws Exception {
            given(commentService.getPostIdByCommentId(1)).willReturn(3);
            given(commentService.delete(1, "testuser"))
                    .willThrow(new AccessDeniedException("작성자만 접근할 수 있습니다."));

            mockMvc.perform(post("/comment/delete/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error/403"))
                    .andExpect(model().attribute("errorMessage", "작성자만 접근할 수 있습니다."));

            then(commentService).should().getPostIdByCommentId(1);
            then(commentService).should().delete(1, "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("존재하지 않는 댓글이면 예외가 발생한다")
        void shouldThrowException_whenCommentDoesNotExistOnDelete() throws Exception {
            given(commentService.getPostIdByCommentId(1))
                    .willThrow(new DataNotFoundException("comment not found"));

            mockMvc.perform(post("/comment/delete/1")
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(view().name("error/404"))
                    .andExpect(model().attribute("errorMessage", "comment not found"));

            then(commentService).should().getPostIdByCommentId(1);
            then(commentService).should(never()).delete(anyInt(), anyString());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 댓글 삭제 요청에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAttemptsToDeleteComment() throws Exception {
            mockMvc.perform(post("/comment/delete/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
