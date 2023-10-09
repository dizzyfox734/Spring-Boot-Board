package dizzyfox734.springbootboard.controller;

import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.controller.dto.UserModifyDto;
import dizzyfox734.springbootboard.domain.user.User;
import dizzyfox734.springbootboard.service.MailService;
import dizzyfox734.springbootboard.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

import static dizzyfox734.springbootboard.common.utils.constants.ResponseConstants.CREATED;
import static dizzyfox734.springbootboard.common.utils.constants.ResponseConstants.OK;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final MailService mailService;

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

        if (userService.validateDuplicateEmail(signupDto.getEmail())) {
            bindingResult.reject("signupFailed", "이미 등록된 이메일입니다.");
            return "user/signup";
        }

        userService.create(signupDto);

        return "redirect:/";
    }

    @PostMapping("/signup/sendMail")
    public ResponseEntity<Void> sendMail(@RequestBody Map<String, String> map) throws Exception {
        mailService.sendMail(map.get("email"));

        return CREATED;
    }

    @PostMapping("/signup/confirm")
    public ResponseEntity<Void> mailVerification(@RequestBody Map<String, String> map) throws Exception {
        mailService.verifyMail(map.get("code"));

        return OK;
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

        return "redirect:/user/logout";
    }
}
