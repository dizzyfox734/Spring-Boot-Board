package dizzyfox734.springbootboard.post.controller;

import dizzyfox734.springbootboard.global.exception.AccessDeniedException;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.global.utils.MarkdownUtil;
import dizzyfox734.springbootboard.member.domain.Authority;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.post.domain.Post;
import dizzyfox734.springbootboard.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@DisplayName("PostController")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

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
                                    "/post/create",
                                    "/post/modify/**",
                                    "/post/delete/**"
                            ).authenticated()

                            // 그 외 요청은 일단 허용하지 않음
                            .anyRequest().denyAll()
                    )
                    .formLogin(form -> form.disable());

            return http.build();
        }
    }

    @BeforeEach
    void setUp() {
        Mockito.reset(postService);
    }

    @Nested
    @DisplayName("GET /post/list")
    class ListGetTest {

        @Test
        @WithAnonymousUser
        @DisplayName("게시글 목록 페이지를 조회할 수 있다")
        void shouldReturnPostListPage_whenAnonymousUserAccessesList() throws Exception {
            Page<Post> paging = new PageImpl<>(List.of());
            given(postService.findPosts(0, "")).willReturn(paging);

            mockMvc.perform(get("/post/list"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("post/list"))
                    .andExpect(model().attribute("paging", paging))
                    .andExpect(model().attribute("kw", ""));

            then(postService).should().findPosts(0, "");
        }

        @Test
        @WithAnonymousUser
        @DisplayName("page와 검색어가 주어지면 해당 조건으로 게시글 목록을 조회한다")
        void shouldReturnFilteredPostList_whenPageAndKeywordAreGiven() throws Exception {
            Page<Post> paging = new PageImpl<>(List.of());
            given(postService.findPosts(2, "test")).willReturn(paging);

            mockMvc.perform(get("/post/list")
                            .param("page", "2")
                            .param("kw", "test"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("post/list"))
                    .andExpect(model().attribute("paging", paging))
                    .andExpect(model().attribute("kw", "test"));

            then(postService).should().findPosts(2, "test");
        }
    }

    @Nested
    @DisplayName("GET /post/detail/{postId}")
    class DetailTest {

        @Test
        @WithAnonymousUser
        @DisplayName("존재하는 게시글이면 상세 페이지를 조회할 수 있다")
        void shouldReturnPostDetailPage_whenPostExists() throws Exception {
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
            ReflectionTestUtils.setField(post, "id", 1);
            ReflectionTestUtils.setField(post, "commentList", List.of());
            ReflectionTestUtils.setField(post, "createdDate", java.time.LocalDateTime.now());

            given(postService.getPost(1)).willReturn(post);

            // Thymeleaf 렌더링 깨짐 방지
            given(markdownUtil.markdown(anyString())).willReturn("<p>test content</p>");

            mockMvc.perform(get("/post/detail/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("post/detail"))
                    .andExpect(model().attribute("post", post));

            then(postService).should().getPost(1);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("존재하지 않는 게시글이면 404 예외가 발생한다")
        void shouldThrowException_whenPostDoesNotExist() throws Exception {
            given(postService.getPost(1))
                    .willThrow(new DataNotFoundException("Post not found"));

            mockMvc.perform(get("/post/detail/1"))
                    .andExpect(status().isNotFound())
                    .andExpect(view().name("error/404"))
                    .andExpect(model().attribute("errorMessage", "Post not found"));

            then(postService).should().getPost(1);
        }
    }

    @Nested
    @DisplayName("GET /post/create")
    class CreateGetTest {

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 게시글 작성 페이지를 조회할 수 있다")
        void shouldReturnCreatePage_whenAuthenticatedUserAccesses() throws Exception {
            mockMvc.perform(get("/post/create"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("post/form"))
                    .andExpect(model().attribute("isModify", false));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 게시글 작성 페이지에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAccessesCreatePage() throws Exception {
            mockMvc.perform(get("/post/create"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /post/create")
    class CreatePostTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("유효한 입력이면 게시글을 생성하고 목록으로 리다이렉트한다")
        void shouldCreatePostAndRedirectToList_whenValidInputIsGiven() throws Exception {
            mockMvc.perform(post("/post/create")
                            .with(csrf())
                            .param("title", "test title")
                            .param("content", "test content"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/post/list"));

            then(postService).should().create("test title", "test content", "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("입력값 검증에 실패하면 작성 페이지를 다시 보여준다")
        void shouldReturnCreatePage_whenValidationFails() throws Exception {
            mockMvc.perform(post("/post/create")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("post/form"))
                    .andExpect(model().attribute("isModify", false));

            then(postService).should(never()).create(anyString(), anyString(), anyString());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 게시글 생성 요청에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAttemptsToCreatePost() throws Exception {
            mockMvc.perform(post("/post/create")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /post/modify/{postId}")
    class ModifyGetTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자는 게시글 수정 페이지를 조회할 수 있다")
        void shouldReturnModifyPage_whenRequesterIsAuthor() throws Exception {
            Post post = mock(Post.class);
            given(postService.getPostForModify(1, "testuser")).willReturn(post);
            given(post.getTitle()).willReturn("test title");
            given(post.getContent()).willReturn("test content");

            mockMvc.perform(get("/post/modify/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("post/form"))
                    .andExpect(model().attribute("isModify", true))
                    .andExpect(model().attribute("postDto", hasProperty("title", is("test title"))))
                    .andExpect(model().attribute("postDto", hasProperty("content", is("test content"))));

            then(postService).should().getPostForModify(1, "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자가 아니면 접근 예외가 발생한다")
        void shouldThrowAccessDeniedException_whenRequesterIsNotAuthor() throws Exception {
            given(postService.getPostForModify(1, "testuser"))
                    .willThrow(new AccessDeniedException("작성자만 접근할 수 있습니다."));

            mockMvc.perform(get("/post/modify/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error/403"))
                    .andExpect(model().attribute("errorMessage", "작성자만 접근할 수 있습니다."));

            then(postService).should().getPostForModify(1, "testuser");
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 게시글 수정 페이지에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAccessesModifyPage() throws Exception {
            mockMvc.perform(get("/post/modify/1"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /post/modify/{postId}")
    class ModifyPostTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("유효한 입력이면 게시글을 수정하고 상세 페이지로 리다이렉트한다")
        void shouldModifyPostAndRedirectToDetail_whenValidInputIsGiven() throws Exception {
            mockMvc.perform(post("/post/modify/1")
                            .with(csrf())
                            .param("title", "test title")
                            .param("content", "test content"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/post/detail/1"));

            then(postService).should().modify(1, "test title", "test content", "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("입력값 검증에 실패하면 수정 페이지를 다시 보여준다")
        void shouldReturnModifyPage_whenValidationFails() throws Exception {
            mockMvc.perform(post("/post/modify/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("post/form"))
                    .andExpect(model().attribute("isModify", true))
                    .andExpect(model().hasErrors());

            then(postService).should(never()).modify(anyInt(), anyString(), anyString(), anyString());
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자가 아니면 수정 요청 시 예외가 발생한다")
        void shouldThrowAccessDeniedException_whenRequesterIsNotAuthorForModify() throws Exception {
            given(postService.modify(1, "test title", "test content", "testuser"))
                    .willThrow(new AccessDeniedException("작성자만 접근할 수 있습니다."));

            mockMvc.perform(post("/post/modify/1")
                            .with(csrf())
                            .param("title", "test title")
                            .param("content", "test content"))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error/403"))
                    .andExpect(model().attribute("errorMessage", "작성자만 접근할 수 있습니다."));

            then(postService).should().modify(1, "test title", "test content", "testuser");
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 게시글 수정 요청에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAttemptsToModifyPost() throws Exception {
            mockMvc.perform(post("/post/modify/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /post/delete/{postId}")
    class DeletePostTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자는 게시글을 삭제하고 목록으로 리다이렉트한다")
        void shouldDeletePostAndRedirectToList_whenRequesterIsAuthor() throws Exception {
            mockMvc.perform(post("/post/delete/1")
                            .with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/post/list"));

            then(postService).should().delete(1, "testuser");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("작성자가 아니면 삭제 요청 시 예외가 발생한다")
        void shouldThrowAccessDeniedException_whenRequesterIsNotAuthorForDelete() throws Exception {
            given(postService.delete(1, "testuser"))
                    .willThrow(new AccessDeniedException("작성자만 접근할 수 있습니다."));

            mockMvc.perform(post("/post/delete/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(view().name("error/403"))
                    .andExpect(model().attribute("errorMessage", "작성자만 접근할 수 있습니다."));

            then(postService).should().delete(1, "testuser");
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 게시글 삭제 요청에 접근할 수 없다")
        void shouldDenyAccess_whenAnonymousUserAttemptsToDeletePost() throws Exception {
            mockMvc.perform(post("/post/delete/1")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
