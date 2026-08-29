package com.minseok.devboard;

import com.minseok.devboard.global.resolver.LoginMemberId;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home(final Model model,
                       final @Nullable @LoginMemberId(required = false) Long loginMemberId) {
        model.addAttribute("isLogin",
                           loginMemberId != null);
        
        return "home";
    }
}
