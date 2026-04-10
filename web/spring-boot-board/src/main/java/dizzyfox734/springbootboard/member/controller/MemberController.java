package dizzyfox734.springbootboard.member.controller;

import dizzyfox734.springbootboard.global.exception.DataNotFoundException;
import dizzyfox734.springbootboard.mail.exception.MailMessageBuildException;
import dizzyfox734.springbootboard.mail.exception.MailSendException;
import dizzyfox734.springbootboard.mail.service.MailCertificationService;
import dizzyfox734.springbootboard.member.controller.dto.*;
import dizzyfox734.springbootboard.member.domain.Member;
import dizzyfox734.springbootboard.member.exception.DuplicateEmailException;
import dizzyfox734.springbootboard.member.exception.DuplicateUsernameException;
import dizzyfox734.springbootboard.member.exception.EmailVerificationException;
import dizzyfox734.springbootboard.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

import static dizzyfox734.springbootboard.global.utils.constants.ResponseConstants.CREATED;

@RequestMapping("/member")
@RequiredArgsConstructor
@Controller
public class MemberController {

    private final MemberService memberService;
    private final MailCertificationService mailCertificationService;

    @PreAuthorize("isAnonymous()")
    @GetMapping("/register")
    public String register(RegisterAgreementDto registerAgreementDto) {
        return "member/register";
    }

    /**
     * 이용약관 동의 체크 후 회원가입 페이지로 리다이렉트
     *
     * @param registerAgreementDto 이용약관 동의 DTO
     * @return 리다이렉트할 회원가입 페이지
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    public String register(@Valid RegisterAgreementDto registerAgreementDto,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "member/register";
        }

        return "redirect:/member/signup";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/signup")
    public String signup(SignupDto signupDto) {
        return "member/signup";
    }

    /**
     * 회원 가입 처리
     *
     * @param signupDto 회원 가입 DTO
     * @param bindingResult 검증 결과
     * @return 회원 가입 후 리다이렉트 경로
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/signup")
    public String signup(@Valid SignupDto signupDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "member/signup";
        }

        try {
            memberService.create(
                    signupDto.getUsername(),
                    signupDto.getPassword1(),
                    signupDto.getName(),
                    signupDto.getEmail(),
                    signupDto.getEmailConfirm()
            );
        } catch (DuplicateUsernameException e) {
            bindingResult.rejectValue("username", "duplicate", e.getMessage());
            return "member/signup";
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "duplicate", e.getMessage());
            return "member/signup";
        } catch (EmailVerificationException e) {
            bindingResult.rejectValue("emailConfirm", "emailConfirmInCorrect", e.getMessage());
            return "member/signup";
        }

        return "redirect:/";
    }

    /**
     * 회원 가입용 이메일 인증코드 전송
     *
     * @param request 이메일 정보
     * @return HTTP 응답 상태
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/signup/sendMail")
    public ResponseEntity<Void> sendSignUpMail(@Valid @RequestBody EmailRequest request) {
        try {
            mailCertificationService.sendSignupVerificationCode(request.getEmail());
            return CREATED;
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/find/id")
    public String findId(FindIdDto findIdDto) {
        return "member/findId";
    }

    /**
     * 아이디 찾기 요청 처리
     *
     * @param findIdDto 아이디 찾기 DTO
     * @param bindingResult 검증 결과
     * @param redirectAttributes 리다이렉트 전달값
     * @return 결과 페이지
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/find/id")
    public String findId(@Valid FindIdDto findIdDto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/findId";
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

    @PreAuthorize("isAnonymous()")
    @GetMapping("/find/pwd")
    public String findPwd(FindPwdDto findPwdDto) {
        return "member/findPwd";
    }

    /**
     * 비밀번호 찾기 요청 처리
     *
     * @param findPwdDto 비밀번호 찾기 DTO
     * @param redirectAttributes 리다이렉트 전달값
     * @return 결과 페이지
     */
    @PreAuthorize("isAnonymous()")
    @PostMapping("/reset/pwd")
    public String resetPwd(@Valid FindPwdDto findPwdDto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "member/findPwd";
        }

        boolean existsMember = memberService.existsForPasswordReset(
                findPwdDto.getName(),
                findPwdDto.getEmail(),
                findPwdDto.getUsername()
        );

        if (!existsMember) {
            redirectAttributes.addAttribute("error", "해당 정보로 회원을 찾을 수 없습니다.");
            return "redirect:/member/find/pwd";
        }

        try {
            memberService.resetPasswordAndSendEmail(
                    findPwdDto.getName(),
                    findPwdDto.getEmail(),
                    findPwdDto.getUsername()
            );
        } catch (MailSendException | MailMessageBuildException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/member/find/pwd";
        }

        return "redirect:/member/login";
    }

    @PreAuthorize("isAnonymous()")
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
     * @param memberModifyDto 회원 정보 수정 DTO
     * @param bindingResult 검증 결과
     * @param principal 인증된 회원 정보
     * @return 수정 완료 후 리다이렉트 경로
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify")
    public String update(@Valid MemberModifyDto memberModifyDto,
                         BindingResult bindingResult,
                         Principal principal,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("username", principal.getName());
            return "member/info";
        }

        Member member = memberService.getMember(principal.getName());
        memberService.modify(member, memberModifyDto.getPassword1());

        return "redirect:/member/logout";
    }
}
