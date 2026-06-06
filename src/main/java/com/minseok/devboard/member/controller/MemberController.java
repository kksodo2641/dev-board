package com.minseok.devboard.member.controller;

import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.exception.DuplicateEmailException;
import com.minseok.devboard.member.exception.DuplicateNicknameException;
import com.minseok.devboard.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    
    private static String resolveView(final String viewName) {
        assert (viewName != null);
        return "member/" + viewName;
    }
}

















