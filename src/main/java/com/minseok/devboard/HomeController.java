package com.minseok.devboard;

import com.minseok.devboard.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

@Controller
@RequiredArgsConstructor
public class HomeController {
    
    private final MemberRepository memberRepository;
    
    @GetMapping("/")
    public String home(final Model model,
                       final @SessionAttribute(name = LOGIN_MEMBER_ID,
                                               required = false) Long loginMemberId) {
        if (loginMemberId != null) {
            model.addAttribute("isLogin",
                               memberRepository.existsById(loginMemberId));
        }
        
        return "home";
    }
}

















