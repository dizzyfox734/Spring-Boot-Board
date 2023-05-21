package dizzyfox734.springbootboard.controller;

import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
