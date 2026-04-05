package dizzyfox734.springbootboard.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.mail.exception.MailMessageBuildException;
import dizzyfox734.springbootboard.mail.exception.MailSendException;
import dizzyfox734.springbootboard.mail.service.MailCertificationService;
import dizzyfox734.springbootboard.member.controller.dto.SignupDto;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.exception.DuplicateEmailException;
import dizzyfox734.springbootboard.member.exception.DuplicateUsernameException;
import dizzyfox734.springbootboard.member.exception.EmailVerificationException;
import dizzyfox734.springbootboard.member.exception.PasswordMismatchException;
import dizzyfox734.springbootboard.member.service.MemberService;
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
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(MemberController.class)
@DisplayName("MemberController")
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @MockBean
    private MailCertificationService mailCertificationService;

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/member/login",
                                    "/member/register",
                                    "/member/signup",
                                    "/member/find/id",
                                    "/member/find/pwd",
                                    "/member/reset/pwd",
                                    "/member/signup/sendMail"
                            ).anonymous()
                            .anyRequest().authenticated()
                    )
                    .formLogin(form -> form.disable());

            return http.build();
        }
    }

    @Nested
    @DisplayName("GET /member/register")
    class RegisterGetTest {

        @Test
        @WithAnonymousUser
        @DisplayName("비회원은 회원가입 약관 동의 페이지를 조회할 수 있다")
        void showRegisterPage_whenAnonymous() throws Exception {
            mockMvc.perform(get("/member/register"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/register"));
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 회원가입 약관 동의 페이지에 접근할 수 없다")
        void denyRegisterPage_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/member/register"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /member/register")
    class RegisterPostTest {

        @Test
        @WithAnonymousUser
        @DisplayName("약관 동의 값이 유효하면 회원가입 페이지로 리다이렉트한다")
        void redirectToSignup_whenRegisterAgreementIsValid() throws Exception {
            mockMvc.perform(post("/member/register")
                            .with(csrf())
                            .param("agreeTermsOfService", "true")
                            .param("agreePrivacyPolicy", "true"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/member/signup"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("약관 동의 입력값 검증에 실패하면 약관 동의 페이지를 다시 보여준다")
        void returnRegisterPage_whenRegisterAgreementIsInvalid() throws Exception {
            mockMvc.perform(post("/member/register")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/register"))
                    .andExpect(model().hasErrors());

        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 약관 동의 제출 요청에 접근할 수 없다")
        void denyRegisterSubmit_whenAuthenticated() throws Exception {
            mockMvc.perform(post("/member/register")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /member/signup")
    class SignupGetTest {

        @Test
        @WithAnonymousUser
        @DisplayName("비회원은 회원가입 페이지를 조회할 수 있다")
        void showSignupPage_whenAnonymous() throws Exception {
            mockMvc.perform(get("/member/signup"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/signup"));
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 회원가입 페이지에 접근할 수 없다")
        void denySignupPage_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/member/signup"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /member/signup")
    class SignupPostTest {
        @BeforeEach
        void setUp() {
            Mockito.reset(memberService);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("회원가입 입력값이 유효하면 메인 페이지로 리다이렉트한다")
        void redirectHome_whenSignupSucceeds() throws Exception {
            given(memberService.create(any(SignupDto.class))).willReturn(1L);

            mockMvc.perform(post("/member/signup")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("password1", "password123")
                            .param("password2", "password123")
                            .param("name", "홍길동")
                            .param("email", "test@example.com")
                            .param("emailConfirm", "A1B2C3D4"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            then(memberService).should().create(argThat(dto ->
                    dto.getUsername().equals("testuser") &&
                            dto.getEmail().equals("test@example.com")
            ));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("회원가입 입력값 검증에 실패하면 회원가입 페이지를 다시 보여준다")
        void returnSignupPage_whenSignupDtoIsInvalid() throws Exception {
            mockMvc.perform(post("/member/signup")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/signup"))
                    .andExpect(model().hasErrors());

            then(memberService).should(never()).create(any(SignupDto.class));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비밀번호 확인 불일치 예외가 발생하면 password2 필드 에러와 함께 회원가입 페이지를 보여준다")
        void returnSignupPageWithPassword2Error_whenPasswordMismatchExceptionOccurs() throws Exception {
            given(memberService.create(any(SignupDto.class)))
                    .willThrow(new PasswordMismatchException("패스워드가 일치하지 않습니다."));

            mockMvc.perform(post("/member/signup")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("password1", "password123")
                            .param("password2", "password123")
                            .param("name", "홍길동")
                            .param("email", "test@example.com")
                            .param("emailConfirm", "A1B2C3D4"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/signup"))
                    .andExpect(model().attributeHasFieldErrors("signupDto", "password2"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("아이디 중복 예외가 발생하면 username 필드 에러와 함께 회원가입 페이지를 보여준다")
        void returnSignupPageWithUsernameError_whenDuplicateUsernameExceptionOccurs() throws Exception {
            given(memberService.create(any(SignupDto.class)))
                    .willThrow(new DuplicateUsernameException("이미 사용중인 아이디입니다."));

            mockMvc.perform(post("/member/signup")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("password1", "password123")
                            .param("password2", "password123")
                            .param("name", "홍길동")
                            .param("email", "test@example.com")
                            .param("emailConfirm", "A1B2C3D4"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/signup"))
                    .andExpect(model().attributeHasFieldErrors("signupDto", "username"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("이메일 중복 예외가 발생하면 email 필드 에러와 함께 회원가입 페이지를 보여준다")
        void returnSignupPageWithEmailError_whenDuplicateEmailExceptionOccurs() throws Exception {
            given(memberService.create(any(SignupDto.class)))
                    .willThrow(new DuplicateEmailException("이미 사용중인 이메일입니다."));

            mockMvc.perform(post("/member/signup")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("password1", "password123")
                            .param("password2", "password123")
                            .param("name", "홍길동")
                            .param("email", "test@example.com")
                            .param("emailConfirm", "A1B2C3D4"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/signup"))
                    .andExpect(model().attributeHasFieldErrors("signupDto", "email"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("이메일 인증 미완료 예외가 발생하면 emailConfirm 필드 에러와 함께 회원가입 페이지를 보여준다")
        void returnSignupPageWithEmailConfirmError_whenEmailVerificationExceptionOccurs() throws Exception {
            given(memberService.create(any(SignupDto.class)))
                    .willThrow(new EmailVerificationException("이메일 인증이 완료되지 않았습니다."));

            mockMvc.perform(post("/member/signup")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("password1", "password123")
                            .param("password2", "password123")
                            .param("name", "홍길동")
                            .param("email", "test@example.com")
                            .param("emailConfirm", "A1B2C3D4"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/signup"))
                    .andExpect(model().attributeHasFieldErrors("signupDto", "emailConfirm"));
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 회원가입 제출 요청에 접근할 수 없다")
        void denySignupSubmit_whenAuthenticated() throws Exception {
            mockMvc.perform(post("/member/signup")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /member/signup/sendMail")
    class SendSignUpMailTest {

        @BeforeEach
        void setUp() {
            Mockito.reset(mailCertificationService);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("이메일이 전달되면 인증 메일을 전송하고 201 Created를 반환한다")
        void returnCreated_whenSignupMailRequestIsValid() throws Exception {
            doNothing().when(mailCertificationService).sendSignupVerificationCode("test@example.com");

            mockMvc.perform(post("/member/signup/sendMail")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("email", "test@example.com"))))
                    .andExpect(status().isCreated());

            then(mailCertificationService).should().sendSignupVerificationCode("test@example.com");
        }

        @Test
        @WithAnonymousUser
        @DisplayName("이메일이 누락되면 400 Bad Request를 반환한다")
        void returnBadRequest_whenEmailIsMissing() throws Exception {
            mockMvc.perform(post("/member/signup/sendMail")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of())))
                    .andExpect(status().isBadRequest());

            then(mailCertificationService).should(never()).sendSignupVerificationCode(anyString());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("이메일이 빈 값이면 400 Bad Request를 반환한다")
        void returnBadRequest_whenEmailIsBlank() throws Exception {
            mockMvc.perform(post("/member/signup/sendMail")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("email", ""))))
                    .andExpect(status().isBadRequest());

            then(mailCertificationService).should(never()).sendSignupVerificationCode(anyString());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("메일 전송 중 예외 발생 시 500 Internal Server Error를 반환한다")
        void sendMail_exception() throws Exception {
            doThrow(new RuntimeException("fail"))
                    .when(mailCertificationService)
                    .sendSignupVerificationCode(anyString());

            mockMvc.perform(post("/member/signup/sendMail")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of("email", "test@example.com"))))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 회원가입 인증 메일 전송 요청에 접근할 수 없다")
        void denySignupMailRequest_whenAuthenticated() throws Exception {
            mockMvc.perform(post("/member/signup/sendMail")
                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /member/find/id")
    class FindIdGetTest {

        @Test
        @WithAnonymousUser
        @DisplayName("비회원은 아이디 찾기 페이지를 조회할 수 있다")
        void showFindIdPage_whenAnonymous() throws Exception {
            mockMvc.perform(get("/member/find/id"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/findId"));
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 아이디 찾기 페이지에 접근할 수 없다")
        void denyFindIdPage_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/member/find/id"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /member/find/id")
    class FindIdPostTest {

        @BeforeEach
        void setUp() {
            Mockito.reset(memberService);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("입력값이 유효하고 회원이 존재하면 username을 flash attribute로 담아 리다이렉트한다")
        void redirectWithUsernameFlashAttribute_whenFindIdSucceeds() throws Exception {
            given(memberService.findUsername("홍길동", "test@example.com"))
                    .willReturn("testuser");

            mockMvc.perform(post("/member/find/id")
                            .with(csrf())
                            .param("name", "홍길동")
                            .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/member/find/id"))
                    .andExpect(flash().attribute("username", "testuser"));

            then(memberService).should().findUsername("홍길동", "test@example.com");
        }

        @Test
        @WithAnonymousUser
        @DisplayName("입력값 검증에 실패하면 아이디 찾기 페이지를 다시 보여준다")
        void returnFindIdPage_whenFindIdDtoIsInvalid() throws Exception {
            mockMvc.perform(post("/member/find/id")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/findId"))
                    .andExpect(model().hasErrors());

            then(memberService).should(never()).findUsername(anyString(), anyString());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("회원 정보를 찾지 못하면 error=true와 함께 리다이렉트한다")
        void redirectWithErrorParameter_whenMemberNotFound() throws Exception {
            given(memberService.findUsername(anyString(), anyString()))
                    .willThrow(new DataNotFoundException("No user found with the provided name and email"));

            mockMvc.perform(post("/member/find/id")
                            .with(csrf())
                            .param("name", "홍길동")
                            .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/member/find/id?error=true"));
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 아이디 찾기 요청에 접근할 수 없다")
        void denyFindIdSubmit_whenAuthenticated() throws Exception {
            mockMvc.perform(post("/member/find/id")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /member/find/pwd")
    class FindPwdGetTest {

        @Test
        @WithAnonymousUser
        @DisplayName("비회원은 비밀번호 찾기 페이지를 조회할 수 있다")
        void showFindPwdPage_whenAnonymous() throws Exception {
            mockMvc.perform(get("/member/find/pwd"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/findPwd"));
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 비밀번호 찾기 페이지에 접근할 수 없다")
        void denyFindPwdPage_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/member/find/pwd"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /member/reset/pwd")
    class ResetPwdTest {

        @BeforeEach
        void setUp() {
            Mockito.reset(memberService);
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비밀번호 재설정 대상 회원이 존재하고 임시 비밀번호 메일 발송에 성공하면 로그인 페이지로 리다이렉트한다")
        void redirectToLogin_whenResetPasswordSucceeds() throws Exception {
            given(memberService.existsForPasswordReset(anyString(), anyString(), anyString()))
                    .willReturn(true);
            given(memberService.resetPasswordAndSendEmail(anyString(), anyString(), anyString()))
                    .willReturn("A1B2C3D4");

            mockMvc.perform(post("/member/reset/pwd")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("name", "홍길동")
                            .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/member/login"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비밀번호 재설정 대상 회원이 없으면 에러 메시지와 함께 비밀번호 찾기 페이지로 리다이렉트한다")
        void redirectToFindPwdWithError_whenResetTargetDoesNotExist() throws Exception {
            given(memberService.existsForPasswordReset(anyString(), anyString(), anyString()))
                    .willReturn(false);

            mockMvc.perform(post("/member/reset/pwd")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("name", "홍길동")
                            .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/member/find/pwd?error=*"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("메일 전송 예외가 발생하면 에러 메시지와 함께 비밀번호 찾기 페이지로 리다이렉트한다")
        void redirectToFindPwdWithError_whenMailSendExceptionOccurs() throws Exception {
            given(memberService.existsForPasswordReset(anyString(), anyString(), anyString()))
                    .willReturn(true);
            given(memberService.resetPasswordAndSendEmail(anyString(), anyString(), anyString()))
                    .willThrow(new MailSendException("이메일 전송에 실패했습니다.", new RuntimeException("SMTP Error")));

            mockMvc.perform(post("/member/reset/pwd")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("name", "홍길동")
                            .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/member/find/pwd?error=*"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("메일 본문 생성 예외가 발생하면 에러 메시지와 함께 비밀번호 찾기 페이지로 리다이렉트한다")
        void redirectToFindPwdWithError_whenMailMessageBuildExceptionOccurs() throws Exception {
            given(memberService.existsForPasswordReset(anyString(), anyString(), anyString()))
                    .willReturn(true);
            given(memberService.resetPasswordAndSendEmail(anyString(), anyString(), anyString()))
                    .willThrow(new MailMessageBuildException("이메일 메시지 생성에 실패했습니다.", new RuntimeException("mime error")));

            mockMvc.perform(post("/member/reset/pwd")
                            .with(csrf())
                            .param("username", "testuser")
                            .param("name", "홍길동")
                            .param("email", "test@example.com"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/member/find/pwd?error=*"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("입력값 검증 실패 시 어떻게 동작하는지 확인")
        void resetPwd_invalidInput() throws Exception {
            mockMvc.perform(post("/member/reset/pwd")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/findPwd"))
                    .andExpect(model().hasErrors());
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 비밀번호 재설정 요청에 접근할 수 없다")
        void denyResetPasswordRequest_whenAuthenticated() throws Exception {
            mockMvc.perform(post("/member/reset/pwd")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /member/login")
    class LoginTest {

        @Test
        @WithAnonymousUser
        @DisplayName("비회원은 로그인 페이지를 조회할 수 있다")
        void showLoginPage_whenAnonymous() throws Exception {
            mockMvc.perform(get("/member/login"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/login"));
        }

        @Test
        @WithMockUser
        @DisplayName("로그인 사용자는 로그인 페이지에 접근할 수 없다")
        void denyLoginPage_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/member/login"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /member/info")
    class InfoTest {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("로그인 사용자는 회원정보 페이지를 조회할 수 있고 모델에 username이 담긴다")
        void showMemberInfoPage_whenAuthenticated() throws Exception {
            mockMvc.perform(get("/member/info"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/info"))
                    .andExpect(model().attribute("username", "testuser"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 회원정보 페이지에 접근할 수 없다")
        void denyMemberInfoPage_whenAnonymous() throws Exception {
            mockMvc.perform(get("/member/info"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /member/modify")
    class UpdateTest {

        @BeforeEach
        void setUp() {
            Mockito.reset(memberService);
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("비밀번호 수정 성공 시 로그아웃으로 리다이렉트되고 서비스가 호출된다")
        void modifySuccess() throws Exception {
            Member member = mock(Member.class);
            given(memberService.getMember("testuser")).willReturn(member);

            mockMvc.perform(post("/member/modify")
                            .with(csrf())
                            .param("password1", "newPassword123")
                            .param("password2", "newPassword123"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/member/logout"));

            then(memberService).should().getMember("testuser");
            then(memberService).should().modify(member, "newPassword123");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("입력값 검증에 실패하면 회원정보 페이지를 다시 보여준다")
        void returnMemberInfoPage_whenMemberModifyDtoIsInvalid() throws Exception {
            mockMvc.perform(post("/member/modify")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/info"))
                    .andExpect(model().hasErrors());

            then(memberService).should(never()).getMember(anyString());
            then(memberService).should(never()).modify(any(Member.class), anyString());
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("비밀번호 확인이 일치하지 않으면 password2 필드 에러와 함께 회원정보 페이지를 보여준다")
        void returnMemberInfoPageWithPassword2Error_whenPasswordsDoNotMatch() throws Exception {
            mockMvc.perform(post("/member/modify")
                            .with(csrf())
                            .param("password1", "newPassword123")
                            .param("password2", "differentPassword123"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("member/info"))
                    .andExpect(model().attributeHasFieldErrors("memberModifyDto", "password2"));

            then(memberService).should(never()).getMember(anyString());
            then(memberService).should(never()).modify(any(Member.class), anyString());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("비로그인 사용자는 회원정보 수정 요청에 접근할 수 없다")
        void denyModifyRequest_whenAnonymous() throws Exception {
            mockMvc.perform(post("/member/modify")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
