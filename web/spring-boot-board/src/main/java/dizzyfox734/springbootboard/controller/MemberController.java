package dizzyfox734.springbootboard.controller;

import dizzyfox734.springbootboard.controller.dto.FindIdDto;
import dizzyfox734.springbootboard.controller.dto.FindPwdDto;
import dizzyfox734.springbootboard.controller.dto.SignupDto;
import dizzyfox734.springbootboard.controller.dto.MemberModifyDto;
import dizzyfox734.springbootboard.domain.member.Member;
import dizzyfox734.springbootboard.exception.DataNotFoundException;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

import static dizzyfox734.springbootboard.common.utils.constants.ResponseConstants.*;

@RequestMapping("/member")
@RequiredArgsConstructor
@Controller
public class MemberController {

    private final MemberService memberService;
    private final MailService mailService;

    @PreAuthorize("isAnonymous")
    @GetMapping("/signup")
    public String signup(SignupDto signupDto) {
        return "member/signup";
    }

    /**
     * 회원 가입을 처리하고 적절한 페이지로 리다이렉트
     *
     * @param signupDto 회원 가입에 필요한 데이터를 담은 DTO
     * @param bindingResult 폼 검증 결과
     * @return 회원 가입 후 리다이렉트할 페이지
     */
    @PreAuthorize("isAnonymous")
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
        }

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

    /**
     * 회원 가입을 위한 이메일 인증 코드 전송
     *
     * @param map 이메일 정보가 담긴 Map
     * @return HTTP 응답 상태
     * @throws Exception 이메일 전송 중 발생할 수 있는 예외
     */
    @PreAuthorize("isAnonymous")
    @PostMapping("/signup/sendMail")
    public ResponseEntity<Void> sendSignUpMail(@RequestBody Map<String, String> map) throws Exception {
        String email = map.get("email");
        if (email.isEmpty()) {
            return BAD_REQUEST;
        }
        mailService.sendSignUpCheckCodeMail(map.get("email"));

        return CREATED;
    }

    @PreAuthorize("isAnonymous")
    @GetMapping("/find/id")
    public String findId(FindIdDto findIdDto) {
        return "member/findId";
    }

    @PreAuthorize("isAnonymous")
    @GetMapping("/find/pwd")
    public String findPwd(FindPwdDto findPwdDto) {
        return "member/findPwd";
    }

    /**
     * 아이디 찾기 요청 처리
     *
     * @param findIdDto 아이디 찾기에 필요한 데이터를 담은 DTO
     * @param bindingResult 폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 속성
     * @return 찾은 아이디 또는 오류를 담은 아이디 찾기 페이지
     */
    @PreAuthorize("isAnonymous")
    @PostMapping("/find/id")
    public String findId(@Valid FindIdDto findIdDto, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/find/id";
        }

        try {
            String username = memberService.findUsername(findIdDto.getName(), findIdDto.getEmail());

            redirectAttributes.addFlashAttribute("username", username);
            return "redirect:/member/find/id";
        } catch (DataNotFoundException e) {
            redirectAttributes.addAttribute("error", true);
            return "redirect:/member/find/id";
        }
    }

    /**
     * 비밀번호 찾기 요청 처리
     *
     * @param findPwdDto 비밀번호 찾기에 필요한 데이터를 담은 DTO
     * @param redirectAttributes 리다이렉트 시 전달할 속성
     * @return 비밀번호 찾기 결과 페이지
     */
    @PreAuthorize("isAnonymous")
    @PostMapping("/reset/pwd")
    public String resetPwd(@Valid FindPwdDto findPwdDto, RedirectAttributes redirectAttributes) {
        boolean existEmail = memberService.existEmail(findPwdDto.getName(), findPwdDto.getEmail(), findPwdDto.getUsername());
        if (!existEmail) {
            redirectAttributes.addAttribute("error", "해당 정보로 회원를 찾을 수 없습니다.");
            return "redirect:/member/find/pwd";
        }

        try {
            String temporaryPwd = memberService.resetPasswordAndSendEmail(findPwdDto.getName(), findPwdDto.getEmail(), findPwdDto.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/member/find/pwd";
        }

        return "redirect:/member/login";
    }

    @PreAuthorize("isAnonymous")
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
     * 회원 정보 수정
     * (현재 비밀번호만 수정 가능)
     *
     * @param memberModifyDto 회원 정보 수정에 필요한 데이터를 담은 DTO
     * @param bindingResult 폼 검증 결과
     * @param principal 인증된 회원 정보
     * @return 수정 완료 후 리다이렉트할 페이지
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
