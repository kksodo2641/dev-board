package com.minseok.devboard.member.controller;

import com.minseok.devboard.member.dto.request.LoginRequest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.exception.LoginFailedException;
import com.minseok.devboard.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

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
    public String loginForm(final @ModelAttribute LoginRequest loginRequest) {
        return resolveView("login");
    }
    
    @PostMapping("/login")
    public String login(final @Valid @ModelAttribute LoginRequest loginRequest,
                        final BindingResult bindingResult,
                        final HttpSession session) {
        if (bindingResult.hasErrors()) {
            return resolveView("login");
        }
        
        try {
            final Long loginMemberId = memberService.login(loginRequest);
            session.setAttribute(LOGIN_MEMBER_ID, loginMemberId);
            
        } catch (final LoginFailedException e) {
            bindingResult.reject("loginFailed", e.getMessage());
            return resolveView("login");
        }
        
        return "redirect:/";
    }
    
    @PostMapping("/logout")
    public String logout(final HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    
    private static String resolveView(final String viewName) {
        assert (viewName != null);
        return "member/" + viewName;
    }
}

















