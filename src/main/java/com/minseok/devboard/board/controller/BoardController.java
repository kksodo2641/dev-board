package com.minseok.devboard.board.controller;

import com.minseok.devboard.board.dto.request.UpdateBoardRequest;
import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.dto.response.BoardPageResponse;
import com.minseok.devboard.board.dto.response.BoardUpdateResponse;
import com.minseok.devboard.board.service.BoardService;
import com.minseok.devboard.global.interceptor.PublicAccess;
import com.minseok.devboard.global.resolver.LoginMemberId;
import com.minseok.devboard.member.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/boards")
public class BoardController {
    
    private static final String VIEWED_BOARDS_COOKIE = "viewedBoards";
    private static final String VIEWED_BOARDS_SEPARATOR = ":";
    private static final int VIEW_COOKIE_MAX_AGE = 60 * 60 * 24; // 24시간
    
    private final MemberService memberService;
    private final BoardService boardService;
    
    /**
     * 게시글 작성 폼
     */
    @GetMapping("/write")
    public String writeBoardForm(final @LoginMemberId Long memberId,
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
    public String writeBoard(final @LoginMemberId Long memberId,
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
    @PublicAccess
    @GetMapping("/{boardId}")
    public String detail(
            final @Nullable @LoginMemberId(required = false) Long loginMemberId,
            final @CookieValue(name = VIEWED_BOARDS_COOKIE, required = false) String viewedBoards,
            final @PathVariable Long boardId,
            final Model model,
            final HttpServletResponse httpResponse) {
        
        increaseViewCountIfNeeded(boardId, viewedBoards, httpResponse);
        
        final BoardDetailResponse boardDetailResponse = boardService.readBoard(boardId);
        model.addAttribute("board", boardDetailResponse);
        
        final boolean isLogin = loginMemberId != null;
        model.addAttribute("isLogin", isLogin);
        
        boolean canEdit = false;
        boolean canDelete = false;
        
        if (isLogin) {
            final boolean isWriter = boardDetailResponse.getWriterId()
                                                        .equals(loginMemberId);
            canEdit = isWriter;
            canDelete = isWriter || memberService.isAdmin(loginMemberId);
        }
        
        model.addAttribute("canEdit", canEdit);
        model.addAttribute("canDelete", canDelete);
        
        return resolveView("detail");
    }
    
    /**
     * 게시글 목록(페이지) 조회
     * page(query param): 1-base
     */
    @PublicAccess
    @GetMapping
    public String boardList(
            final @Nullable @LoginMemberId(required = false) Long loginMemberId,
            final @RequestParam(defaultValue = "1") int page,
            final Model model) {
        
        final BoardPageResponse pageResponse = boardService.getBoardPage(page);
        
        model.addAttribute("page", pageResponse);
        model.addAttribute("isLogin", loginMemberId != null);
        
        return resolveView("list");
    }
    
    /**
     * 게시글 수정 폼
     */
    @GetMapping("/{boardId}/edit")
    public String editForm(final @LoginMemberId Long loginMemberId,
                           final @PathVariable Long boardId,
                           final Model model) {
        final BoardUpdateResponse response = boardService.getBoardForUpdate(loginMemberId, boardId);
        
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
    
    private void increaseViewCountIfNeeded(final Long boardId,
                                           final String viewedBoardsOrNull,
                                           final HttpServletResponse response) {
        assert (boardId != null);
        assert (response != null);
        
        log.info("viewedBoardsOrNull = {}", viewedBoardsOrNull);
        
        final String cookieValue;
        
        if (viewedBoardsOrNull == null) {
            cookieValue = String.valueOf(boardId);
            
        } else {
            final String boardIdStr = String.valueOf(boardId);
            
            for (final String viewedId : viewedBoardsOrNull.split(VIEWED_BOARDS_SEPARATOR)) {
                if (boardIdStr.equals(viewedId)) {
                    return; // 이미 조회한 게시글인 경우
                }
            }
            
            cookieValue = (viewedBoardsOrNull + VIEWED_BOARDS_SEPARATOR + boardId);
        }
        
        log.info("cookieValue = {}", cookieValue);
        
        final Cookie cookie = new Cookie(VIEWED_BOARDS_COOKIE, cookieValue);
        cookie.setMaxAge(VIEW_COOKIE_MAX_AGE);
        cookie.setPath("/boards"); // 게시판(Board) 기능 전용
        
        response.addCookie(cookie);
        
        boardService.increaseViewCount(boardId);
    }
    
    private static String resolveView(final String viewName) {
        assert (viewName != null);
        return "board/" + viewName;
    }
}
