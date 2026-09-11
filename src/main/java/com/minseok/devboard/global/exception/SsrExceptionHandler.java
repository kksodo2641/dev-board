package com.minseok.devboard.global.exception;

import com.minseok.devboard.HomeController;
import com.minseok.devboard.board.controller.BoardController;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.member.controller.MemberController;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice(assignableTypes = {
        HomeController.class,
        MemberController.class,
        BoardController.class
})
public class SsrExceptionHandler {
    
    private static final String USER_MESSAGE = "userMessage";
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleMethodArgumentTypeMismatch(final Model model) {
        model.addAttribute(USER_MESSAGE,
                           "요청 값의 형식이 올바르지 않습니다.");
        return "error/400";
    }
    
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(final Model model) {
        model.addAttribute(USER_MESSAGE,
                           "요청을 수행할 권한이 없습니다.");
        return "error/403";
    }
    
    @ExceptionHandler(MemberNotFoundException.class)
    public String handleMemberNotFound(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        
        if (session != null) {
            session.invalidate();
        }
        
        return "redirect:/members/login?sessionInvalidated=true";
    }
    
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(BoardNotFoundException.class)
    public String handleBoardNotFound(final Model model) {
        model.addAttribute(USER_MESSAGE,
                           "요청한 게시글을 찾을 수 없습니다.");
        return "error/404";
    }
}
