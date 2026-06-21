package com.minseok.devboard.board.controller;

import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.dto.response.BoardPageResponse;
import com.minseok.devboard.board.service.BoardService;
import com.minseok.devboard.global.resolver.LoginMemberId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
    
    private final BoardService boardService;
    
    /**
     * page(query param): 1-base
     */
    @GetMapping
    public String boardList(
            final @SessionAttribute(value = LOGIN_MEMBER_ID, required = false) Long loginMemberId,
            final @RequestParam(defaultValue = "1") int page,
            final Model model) {
        
        final BoardPageResponse pageResponse = boardService.getBoardPage(page);
        
        model.addAttribute("page", pageResponse);
        model.addAttribute("isLogin", loginMemberId != null);
        
        return resolveView("list");
    }
    
    @GetMapping("/write")
    public String writeForm(final @LoginMemberId Long memberId,
                            final @ModelAttribute WriteBoardRequest request,
                            final Model model) {
        model.addAttribute("categories",
                           boardService.getWritableCategories(memberId));
        return resolveView("write");
    }
    
    @PostMapping("/write")
    public String write(final @LoginMemberId Long memberId,
                        final @Valid @ModelAttribute WriteBoardRequest request,
                        final BindingResult bindingResult,
                        final Model model,
                        final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories",
                               boardService.getWritableCategories(memberId));
            return resolveView("write");
        }
        
        final Long boardId = boardService.writeBoard(memberId, request);
        redirectAttributes.addAttribute("boardId", boardId);
        
        return "redirect:/boards/{boardId}";
    }
    
    @GetMapping("/{boardId}")
    public String detail(final @PathVariable Long boardId,
                         final Model model) {
        final BoardDetailResponse response = boardService.readBoard(boardId);
        model.addAttribute("board", response);
        
        return resolveView("detail");
    }
    
    private static String resolveView(final String viewName) {
        assert (viewName != null);
        return "board/" + viewName;
    }
}

















