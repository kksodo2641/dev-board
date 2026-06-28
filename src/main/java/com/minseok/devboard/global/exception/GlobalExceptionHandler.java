package com.minseok.devboard.global.exception;

import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MemberNotFoundException.class)
    public String handleMemberNotFound(final MemberNotFoundException e) {
        // TODO: 에러 페이지 정책 확정 후, 404(NOT_FOUND) 상태코드 설정 및 오류 페이지 렌더링
        return "redirect:/";
    }
    
    // TODO: BoardNotFoundException handler
    @ExceptionHandler(BoardNotFoundException.class)
    public String handleBoardNotFound(final BoardNotFoundException e) {
        // TODO: 에러 페이지 정책 확정 후, 404(NOT_FOUND) 상태코드 설정 및 오류 페이지 렌더링
        return "redirect:/boards";
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(final AccessDeniedException e) {
        
        // TODO: 에러 페이지 정책 확정 후, 403(Forbidden) 상태코드 설정 및 오류 페이지 렌더링
        return "redirect:/";
    }
}
