package com.minseok.devboard.board.controller;

import com.minseok.devboard.board.dto.request.UpdateBoardRequest;
import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.dto.response.BoardPageResponse;
import com.minseok.devboard.board.dto.response.UpdateBoardResponse;
import com.minseok.devboard.board.service.BoardService;
import com.minseok.devboard.global.resolver.LoginMemberId;
import com.minseok.devboard.member.service.MemberService;
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
    private final MemberService memberService;
    
    /**
     * 게시글 목록(페이지) 조회
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
    
    /**
     * 게시글 작성 폼
     */
    @GetMapping("/write")
    public String writeForm(final @LoginMemberId Long memberId,
                            final @ModelAttribute WriteBoardRequest writeBoardRequest,
                            final Model model) {
        model.addAttribute("categories",
                           boardService.getWritableCategories(memberId));
        return resolveView("write");
    }
    
    /**
     * 게시글 작성
     */
    @PostMapping("/write")
    public String write(final @LoginMemberId Long memberId,
                        final @Valid @ModelAttribute WriteBoardRequest writeBoardRequest,
                        final BindingResult bindingResult,
                        final Model model,
                        final RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories",
                               boardService.getWritableCategories(memberId));
            return resolveView("write");
        }
        
        final Long boardId = boardService.writeBoard(memberId, writeBoardRequest);
        redirectAttributes.addAttribute("boardId", boardId);
        
        return "redirect:/boards/{boardId}";
    }
    
    /**
     * 게시글 상세
     */
    @GetMapping("/{boardId}")
    public String detail(final @SessionAttribute(name = LOGIN_MEMBER_ID,
                                                 required = false) Long loginMemberId,
                         final @PathVariable Long boardId,
                         final Model model) {
        final BoardDetailResponse response = boardService.readBoard(boardId);
        
        boolean canEdit = false;
        boolean canDelete = false;
        
        if (loginMemberId != null) {
            final boolean isWriter = response.getWriterId().equals(loginMemberId);
            canEdit = isWriter;
            canDelete = isWriter || memberService.isAdmin(loginMemberId);
        }
        
        model.addAttribute("board", response);
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("canDelete", canDelete);
        
        return resolveView("detail");
    }
    
    /**
     * 게시글 수정 폼
     */
    @GetMapping("/{boardId}/edit")
    public String editForm(final @LoginMemberId Long loginMemberId,
                           final @PathVariable Long boardId,
                           final Model model) {
        final UpdateBoardResponse response = boardService.getBoardForUpdate(loginMemberId, boardId);
        
        final UpdateBoardRequest request = UpdateBoardRequest.from(response);
        model.addAttribute("updateBoardRequest", request);
        
        model.addAttribute("categories",
                           boardService.getWritableCategories(loginMemberId));
        
        model.addAttribute("boardId", boardId);
        
        return resolveView("edit");
    }
    
    /**
     * 게시글 수정
     */
    @PostMapping("/{boardId}/edit")
    public String edit(final @LoginMemberId Long loginMemberId,
                       final @PathVariable Long boardId,
                       final @Valid @ModelAttribute UpdateBoardRequest updateBoardRequest,
                       final BindingResult bindingResult,
                       final Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories",
                               boardService.getWritableCategories(loginMemberId));
            
            model.addAttribute("boardId", boardId);
            
            return resolveView("edit");
        }
        
        boardService.updateBoard(loginMemberId, boardId, updateBoardRequest);
        
        return "redirect:/boards/{boardId}";
    }
    
    /**
     * 게시글 삭제
     */
    @PostMapping("/{boardId}/delete")
    public String delete(final @LoginMemberId Long loginMemberId,
                         final @PathVariable Long boardId) {
        boardService.deleteBoard(loginMemberId, boardId);
        return "redirect:/boards";
    }
    
    private static String resolveView(final String viewName) {
        assert (viewName != null);
        return "board/" + viewName;
    }
}
