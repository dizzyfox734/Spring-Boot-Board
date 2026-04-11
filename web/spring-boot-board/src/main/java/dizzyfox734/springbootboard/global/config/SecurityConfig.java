package dizzyfox734.springbootboard.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toStaticResources;

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig {

    @Value("${app.security.h2-console.enabled:false}")
    private boolean h2ConsoleEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> {
                    if (h2ConsoleEnabled) {
                        csrf.ignoringRequestMatchers(toH2Console());
                    }
                })

                .headers(headers -> {
                    if (h2ConsoleEnabled) {
                        headers.addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN
                        ));
                    }
                })

                .authorizeHttpRequests(authorize -> {
                    authorize
                            // 정적 리소스
                            .requestMatchers(toStaticResources().atCommonLocations()).permitAll();

                    if (h2ConsoleEnabled) {
                        authorize
                                // H2 콘솔
                                .requestMatchers(toH2Console()).permitAll();
                    }

                    authorize
                            // 공개 페이지
                            .requestMatchers(
                                    "/",
                                    "/main",
                                    "/post/list",
                                    "/post/detail/**"
                            ).permitAll()

                            // 회원 비인증 사용자용 페이지
                            .requestMatchers(
                                    "/member/login",
                                    "/member/register",
                                    "/member/signup",
                                    "/member/find/id",
                                    "/member/find/pwd",
                                    "/member/reset/pwd",
                                    "/member/signup/sendMail"
                            ).permitAll()

                            // 회원 인증 사용자용 페이지
                            .requestMatchers(
                                    "/post/create",
                                    "/post/modify/**",
                                    "/post/delete/**",
                                    "/comment/create/**",
                                    "/comment/modify/**",
                                    "/comment/delete/**",
                                    "/member/info",
                                    "/member/modify",
                                    "/member/logout"
                            ).authenticated()

                            // 그 외 요청은 일단 허용하지 않음
                            .anyRequest().denyAll();
                })

                .formLogin(login -> login
                        .loginPage("/member/login")
                        .loginProcessingUrl("/member/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/member/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout"))
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}
