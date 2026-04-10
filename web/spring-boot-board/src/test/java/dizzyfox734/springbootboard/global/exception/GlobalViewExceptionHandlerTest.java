package dizzyfox734.springbootboard.global.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("GlobalViewExceptionHandler")
class GlobalViewExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExceptionTriggerController())
                .setControllerAdvice(new GlobalViewExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("DataNotFoundException은 404 에러 화면과 상세 메시지로 표현된다")
    void shouldReturnNotFoundErrorView_whenDataNotFoundExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test-exceptions/data-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("errorMessage", "Post not found"));
    }

    @Test
    @DisplayName("AccessDeniedException은 403 에러 화면과 상세 메시지로 표현된다")
    void shouldReturnForbiddenErrorView_whenAccessDeniedExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test-exceptions/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(model().attribute("errorMessage", "작성자만 접근할 수 있습니다."));
    }

    @Test
    @DisplayName("InvalidInputException은 400 에러 화면과 상세 메시지로 표현된다")
    void shouldReturnBadRequestErrorView_whenInvalidInputExceptionIsThrown() throws Exception {
        mockMvc.perform(get("/test-exceptions/invalid-input"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/400"))
                .andExpect(model().attribute("errorMessage", "입력값이 올바르지 않습니다."));
    }

    @Controller
    @RequestMapping("/test-exceptions")
    private static class ExceptionTriggerController {

        @GetMapping("/data-not-found")
        String dataNotFound() {
            throw new DataNotFoundException("Post not found");
        }

        @GetMapping("/access-denied")
        String accessDenied() {
            throw new AccessDeniedException("작성자만 접근할 수 있습니다.");
        }

        @GetMapping("/invalid-input")
        String invalidInput() {
            throw new InvalidInputException("입력값이 올바르지 않습니다.");
        }
    }
}
