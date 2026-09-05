package com.minseok.devboard.member.controller;

import com.minseok.devboard.global.interceptor.PublicAccess;
import com.minseok.devboard.global.resolver.LoginMemberId;
import com.minseok.devboard.member.dto.request.LoginRequest;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.dto.request.UpdateMemberRequest;
import com.minseok.devboard.member.dto.response.MyPageResponse;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.exception.LoginFailedException;
import com.minseok.devboard.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    
    private static final List<Gender> DISPLAY_GENDERS = List.of(Gender.NONE, Gender.MALE, Gender.FEMALE);
    
    private final MemberService memberService;
    
    @ModelAttribute("displayGenders")
    public List<Gender> displayGenders() {
        return DISPLAY_GENDERS;
    }
    
    @PublicAccess
    @GetMapping("/signup")
    public String signupForm(final @ModelAttribute SignupRequest signupRequest) {
        return resolveView("signup");
    }
    
    @PublicAccess
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
    
    @PublicAccess
    @GetMapping("/login")
    public String loginForm(final @ModelAttribute LoginRequest loginRequest) {
        return resolveView("login");
    }
    
    @PublicAccess
    @PostMapping("/login")
    public String login(final @Valid @ModelAttribute LoginRequest loginRequest,
                        final BindingResult bindingResult,
                        final HttpSession session,
                        final @RequestParam(defaultValue = "/") String redirectURL) {
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
        
        return "redirect:" + redirectURL;
    }
    
    @PostMapping("/logout")
    public String logout(final HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    
    @GetMapping("/me")
    public String myPage(final @LoginMemberId Long memberId,
                         final Model model) {
        final MyPageResponse myPageResponse = memberService.getMyPage(memberId);
        model.addAttribute("member", myPageResponse);
        
        return resolveView("myPage");
    }
    
    @GetMapping("/me/edit")
    public String editForm(final @LoginMemberId Long loginMemberId,
                           final @ModelAttribute UpdateMemberRequest updateMemberRequest) {
        final MyPageResponse myPageResponse = memberService.getMyPage(loginMemberId);
        updateMemberRequest.setNickname(myPageResponse.getNickname());
        updateMemberRequest.setGender(myPageResponse.getGender());
        
        return resolveView("edit");
    }
    
    @PostMapping("/me/edit")
    public String edit(final @LoginMemberId Long loginMemberId,
                       final @Valid @ModelAttribute UpdateMemberRequest updateMemberRequest,
                       final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return resolveView("edit");
        }
        
        try {
            memberService.updateProfile(loginMemberId, updateMemberRequest);
            
        } catch (final DuplicateNicknameException e) {
            bindingResult.rejectValue("nickname", "duplicateNickname", e.getMessage());
            return resolveView("edit");
        }
        
        return "redirect:/members/me";
    }
    
    @PostMapping("/me/withdraw")
    public String withdraw(final @LoginMemberId Long loginMemberId,
                           final HttpSession session) {
        memberService.withdraw(loginMemberId);
        session.invalidate();
        
        return "redirect:/";
    }
    
    private static String resolveView(final String viewName) {
        assert (viewName != null);
        return "member/" + viewName;
    }
}
