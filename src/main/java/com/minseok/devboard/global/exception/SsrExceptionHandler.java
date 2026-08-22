package com.minseok.devboard.global.exception;

import com.minseok.devboard.HomeController;
import com.minseok.devboard.board.controller.BoardController;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.member.controller.MemberController;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = {
        HomeController.class,
        MemberController.class,
        BoardController.class
})
public class SsrExceptionHandler {
    
    @ExceptionHandler(MemberNotFoundException.class)
    public String handleMemberNotFound() {
        // TODO: 에러 페이지 정책 확정 후, 404(NOT_FOUND) 상태코드 설정 및 오류 페이지 렌더링
        return "redirect:/";
    }
    
    // TODO: BoardNotFoundException handler
    @ExceptionHandler(BoardNotFoundException.class)
    public String handleBoardNotFound() {
        // TODO: 에러 페이지 정책 확정 후, 404(NOT_FOUND) 상태코드 설정 및 오류 페이지 렌더링
        return "redirect:/boards";
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied() {
        
        // TODO: 에러 페이지 정책 확정 후, 403(Forbidden) 상태코드 설정 및 오류 페이지 렌더링
        return "redirect:/";
    }
}
