package com.minseok.devboard.board.controller;

import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.entity.BoardCategory;
import com.minseok.devboard.board.service.BoardService;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
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
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

import static com.minseok.devboard.global.common.SessionConst.LOGIN_MEMBER_ID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
    
    private final BoardService boardService;
    private final MemberRepository memberRepository;
    
    @GetMapping("/write")
    public String writeForm(final @SessionAttribute(LOGIN_MEMBER_ID) Long memberId,
                            final @ModelAttribute WriteBoardRequest request,
                            final Model model) {
        model.addAttribute("categories", getWritableCategories(memberId));
        return resolveView("write");
    }
    
    @PostMapping("/write")
    public String write(final @SessionAttribute(LOGIN_MEMBER_ID) Long memberId,
                        final @Valid @ModelAttribute WriteBoardRequest request,
                        final BindingResult bindingResult,
                        final Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", getWritableCategories(memberId));
            return resolveView("write");
        }
        
        boardService.writeBoard(memberId, request);
        
        // TODO: 상세 화면(/boards/{boardId} redirect로 변경 예정
        return "redirect:/";
    }
    
    @GetMapping("/{boardId}")
    public String detail(final @PathVariable Long boardId,
                         final Model model) {
        final BoardDetailResponse response = boardService.readBoard(boardId);
        model.addAttribute("board", response);
        
        return resolveView("detail");
    }
    
    private List<BoardCategory> getWritableCategories(final Long memberId) {
        final Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                                              .orElseThrow(MemberNotFoundException::new);
        
        return member.isAdmin()
               ? List.of(BoardCategory.values())
               : BoardCategory.userCategories();
    }
    
    private static String resolveView(final String viewName) {
        assert (viewName != null);
        return "board/" + viewName;
    }
}

















