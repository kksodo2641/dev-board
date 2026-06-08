package com.minseok.devboard.member.controller;

import com.minseok.devboard.member.dto.request.LoginRequest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.dto.response.MyPageResponse;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.exception.LoginFailedException;
import com.minseok.devboard.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    
    private final MemberService memberService;
    
    @GetMapping("/signup")
    public String signupForm(final @ModelAttribute SignupRequest signupRequest) {
        return resolveView("signup");
    }
    
    @PostMapping("/signup")
    public String signup(final @Valid @ModelAttribute SignupRequest signupRequest,
                         final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return resolveView("signup");
        }
        
        try {
            memberService.signup(signupRequest);
            
        } catch (final DuplicateEmailException e) {
            bindingResult.rejectValue("email", "duplicateEmail", e.getMessage());
            return resolveView("signup");
            
        } catch (final DuplicateNicknameException e) {
            bindingResult.rejectValue("nickname", "duplicateNickname", e.getMessage());
            return resolveView("signup");
        }
        
        return "redirect:/";
    }
    
    @GetMapping("/login")
    public String loginForm(final @ModelAttribute LoginRequest loginRequest,
                            final @RequestParam(defaultValue = "/") String redirectURL,
                            final Model model) {
        model.addAttribute("redirectURL", redirectURL);
        return resolveView("login");
    }
    
    @PostMapping("/login")
    public String login(final @Valid @ModelAttribute LoginRequest loginRequest,
                        final BindingResult bindingResult,
                        final HttpSession session,
                        final @RequestParam(defaultValue = "/") String redirectURL,
                        final Model model) {
        
        log.info("login(): redirectURL = {}", redirectURL);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("redirectURL", redirectURL);
            return resolveView("login");
        }
        
        try {
            final Long loginMemberId = memberService.login(loginRequest);
            session.setAttribute(LOGIN_MEMBER_ID, loginMemberId);
            
        } catch (final LoginFailedException e) {
            bindingResult.reject("loginFailed", e.getMessage());
            model.addAttribute("redirectURL", redirectURL);
            
            return resolveView("login");
        }
        
        return "redirect:" + redirectURL;
    }
    
    @PostMapping("/logout")
    public String logout(final HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    
    /**
     * 마이페이지
     */
    @GetMapping("/me")
    public String myPage(final @SessionAttribute(name = LOGIN_MEMBER_ID,
                                                 required = false) Long memberId,
                         final Model model) {
        // 로그인하지 않은 경우
        // - 로그인하도록 리다이렉트
        // - 로그인 시, 다시 마이페이지로 돌아오도록 redirectURL 설정
        if (memberId == null) {
            return "redirect:/members/login?redirectURL=/members/me";
        }
        
        final MyPageResponse myPageResponse = memberService.getMyPage(memberId);
        model.addAttribute("myPageResponse", myPageResponse);
        
        return resolveView("myPage");
    }
    
    private static String resolveView(final String viewName) {
        assert (viewName != null);
        return "member/" + viewName;
    }
}

















