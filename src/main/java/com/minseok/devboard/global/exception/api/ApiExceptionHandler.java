package com.minseok.devboard.global.exception.api;

import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.comment.exception.CommentNotFoundException;
import com.minseok.devboard.comment.exception.ReplyNotAllowedException;
import com.minseok.devboard.global.exception.AccessDeniedException;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static com.minseok.devboard.global.exception.api.ApiErrorCode.ACCESS_DENIED;
import static com.minseok.devboard.global.exception.api.ApiErrorCode.BOARD_NOT_FOUND;
import static com.minseok.devboard.global.exception.api.ApiErrorCode.COMMENT_NOT_FOUND;
import static com.minseok.devboard.global.exception.api.ApiErrorCode.INTERNAL_SERVER_ERROR;
import static com.minseok.devboard.global.exception.api.ApiErrorCode.INVALID_REQUEST;
import static com.minseok.devboard.global.exception.api.ApiErrorCode.MEMBER_NOT_FOUND;
import static com.minseok.devboard.global.exception.api.ApiErrorCode.REPLY_NOT_ALLOWED;
import static com.minseok.devboard.global.exception.api.ApiErrorCode.VALIDATION_ERROR;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {
    
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleMemberNotFound() {
        return createResponse(MEMBER_NOT_FOUND);
    }
    
    @ExceptionHandler(BoardNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBoardNotFound() {
        return createResponse(BOARD_NOT_FOUND);
    }
    
    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCommentNotFound() {
        return createResponse(COMMENT_NOT_FOUND);
    }
    
    @ExceptionHandler(ReplyNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleReplyNotAllowed() {
        return createResponse(REPLY_NOT_ALLOWED);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied() {
        return createResponse(ACCESS_DENIED);
    }
    
    @ExceptionHandler({
            HttpMessageNotReadableException.class,     // body 형식이 잘못된 경우(예: JSON 문법 오류)
            MethodArgumentTypeMismatchException.class  // 인자 타입이 맞지 않는 경우
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidRequest() {
        return createResponse(INVALID_REQUEST);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(
            final MethodArgumentNotValidException ex) {
        
        final ApiErrorCode errorCode = VALIDATION_ERROR;
        final String message = getMessage(ex.getBindingResult(),
                                          errorCode.getMessage());
        
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiErrorResponse.of(errorCode, message));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(final Exception e) {
        log.error("처리되지 않은 예외가 발생했습니다.", e);
        return createResponse(INTERNAL_SERVER_ERROR);
    }
    
    private static ResponseEntity<ApiErrorResponse> createResponse(final ApiErrorCode errorCode) {
        assert (errorCode != null);
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiErrorResponse.from(errorCode));
    }
    
    private static String getMessage(final BindingResult bindingResult,
                                     final String defaultMessage) {
        assert (bindingResult != null);
        assert (StringUtils.hasText(defaultMessage));
        
        final ObjectError validationError = bindingResult.getAllErrors().stream()
                                                         .findFirst()
                                                         .orElse(null);
        if (validationError == null) {
            return defaultMessage;
        }
        
        final String validationMessage = validationError.getDefaultMessage();
        
        return StringUtils.hasText(validationMessage)
               ? validationMessage
               : defaultMessage;
    }
}
