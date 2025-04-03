package dizzyfox734.springbootboard.controller;

import dizzyfox734.springbootboard.controller.dto.FindIdDto;
import dizzyfox734.springbootboard.controller.dto.FindPwdDto;
import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.controller.dto.MemberModifyDto;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.service.MailService;
import dizzyfox734.springbootboard.service.MemberService;
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

import static dizzyfox734.springbootboard.common.utils.constants.ResponseConstants.*;

@RequestMapping("/member")
@RequiredArgsConstructor
@Controller
public class MemberController {

    private final MemberService memberService;
    private final MailService mailService;

    @GetMapping("/signup")
    public String signup(SignupDto signupDto) {
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid SignupDto signupDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "member/signup";
        }

        if (!signupDto.getPassword1().equals(signupDto.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "패스워드가 일치하지 않습니다.");
            return "member/signup";
        }

        if (memberService.validateDuplicateMember(signupDto.getUsername())) {
            bindingResult.reject("signupFailed", "이미 등록된 아이디입니다.");
            return "member/signup";
        };

        if (memberService.validateDuplicateEmail(signupDto.getEmail())) {
            bindingResult.reject("signupFailed", "이미 등록된 이메일입니다.");
            return "member/signup";
        }

        if (!mailService.verifyMail(signupDto.getEmail(), signupDto.getEmailConfirm())) {
            bindingResult.rejectValue("emailConfirm", "emailConfirmInCorrect",
                    "인증코드가 일치하지 않습니다.");
            return "member/signup";
        }

        memberService.create(signupDto);

        return "redirect:/";
    }

    @PostMapping("/signup/sendMail")
    public ResponseEntity<Void> sendMail(@RequestBody Map<String, String> map) throws Exception {
        String email = map.get("email");
        if (email.isEmpty()) {
            return BAD_REQUEST;
        }
        mailService.sendMail(map.get("email"));

        return CREATED;
    }

    @GetMapping("/login")
    public String login() {
        return "member/login";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/info")
    public String info(Model model, Principal principal, MemberModifyDto memberModifyDto) {
        model.addAttribute("username", principal.getName());

        return "member/info";
    }

    /**
     * 유저 정보 변경
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify")
    public String update(@Valid MemberModifyDto memberModifyDto, BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) {
            return "member/info";
        }

        if (!memberModifyDto.getPassword1().equals(memberModifyDto.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "패스워드가 일치하지 않습니다.");
            return "member/info";
        }

        Member member = memberService.getMember(principal.getName());
        memberService.modify(member, memberModifyDto.getPassword1());

        return "redirect:/member/logout";
    }
}
