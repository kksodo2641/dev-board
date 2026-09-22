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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    
    private static final List<Gender> DISPLAY_GENDERS = List.of(Gender.NONE, Gender.MALE, Gender.FEMALE);
    
    private static final String DEFAULT_REDIRECT_URL = "/";
    
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
    public String loginForm(final @ModelAttribute LoginRequest loginRequest,
                            final @RequestParam(defaultValue = "false") boolean sessionInvalidated,
                            final Model model) {
        model.addAttribute("sessionInvalidated", sessionInvalidated);
        return resolveView("login");
    }
    
    @PublicAccess
    @PostMapping("/login")
    public String login(final @Valid @ModelAttribute LoginRequest loginRequest,
                        final BindingResult bindingResult,
                        final HttpSession session,
                        final @RequestParam(defaultValue = DEFAULT_REDIRECT_URL) String redirectURL) {
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
        
        return "redirect:" + resolveRedirectURL(redirectURL);
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
    
    /**
     * 오픈 리다이렉트를 방지하기 위해 로그인 성공 후 이동 경로를
     * 안전한 애플리케이션 내부 경로로 제한한다.
     *
     * <p>
     * 올바른 URI 형식이며 scheme과 authority가 없고,
     * 원본 값과 해석된 path가 {@code /}로 시작하되 {@code //}로 시작하지 않으며
     * 역슬래시와 fragment를 포함하지 않는 경우에만 원본 값을 사용한다.
     * Query 문자열의 내용은 별도로 검사하지 않는다.
     * 그 외의 값은 기본 경로인 {@code /}로 대체한다.
     *
     * @param redirectURL 클라이언트가 요청한 로그인 후 이동 경로
     * @return 검증된 내부 경로 또는 기본 경로({@code /})
     */
    private static String resolveRedirectURL(final String redirectURL) {
        if (redirectURL == null || redirectURL.isBlank()) {
            return DEFAULT_REDIRECT_URL;
        }
        
        try {
            final URI uri = new URI(redirectURL);
            final String path = uri.getPath();
            
            if (uri.isAbsolute()                        // "https:", "http:" 등 scheme 존재
                    || uri.getRawAuthority() != null    // 명시적인 authority 존재
                    || redirectURL.startsWith("//")     // authority 표시 문자열인 //로 시작
                    || path == null                     // 경로 없음
                    || !path.startsWith("/")            // "boards" 등 일반 상대 경로
                    || path.startsWith("//")            // 디코딩된 경로가 이중 슬래시로 시작
                    || path.contains("\\")              // 역슬래시(\) 포함 경로
                    || uri.getRawFragment() != null) {  // 현재 정책에서 필요하지 않은 fragment 존재
                return DEFAULT_REDIRECT_URL;
            }
            
            return redirectURL;
            
        } catch (final URISyntaxException e) {
            return DEFAULT_REDIRECT_URL;
        }
    }
}
