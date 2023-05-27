package dizzyfox734.springbootboard.controller;

import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.controller.dto.UserModifyDto;
import dizzyfox734.springbootboard.domain.user.User;
import dizzyfox734.springbootboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/signup")
    public String signup(SignupDto signupDto) {
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid SignupDto signupDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/signup";
        }

        if (!signupDto.getPassword1().equals(signupDto.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "패스워드가 일치하지 않습니다.");
            return "user/signup";
        }

        if (userService.validateDuplicateUser(signupDto.getUsername())) {
            bindingResult.reject("signupFailed", "이미 등록된 사용자입니다.");
            return "user/signup";
        };

        userService.create(signupDto);

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public String info(Model model, Principal principal, UserModifyDto userModifyDto) {
        model.addAttribute("username", principal.getName());

        return "user/info";
    }

    /**
     * 유저 정보 변경
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify")
    public String update(@Valid UserModifyDto userModifyDto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "user/info";
        }

        if (!userModifyDto.getPassword1().equals(userModifyDto.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "패스워드가 일치하지 않습니다.");
            return "user/info";
        }

        User user = userService.getUser(principal.getName());
        userService.modify(user, userModifyDto.getPassword1());

        return "user/info";
    }
}
