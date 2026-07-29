package com.minseok.devboard.comment.controller;

import com.minseok.devboard.comment.dto.request.UpdateCommentRequest;
import com.minseok.devboard.comment.dto.request.WriteCommentRequest;
import com.minseok.devboard.comment.dto.response.CommentResponse;
import com.minseok.devboard.comment.service.CommentService;
import com.minseok.devboard.global.resolver.LoginMemberId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentApiController {
    
    private final CommentService commentService;
    
    @GetMapping("/boards/{boardId}/comments")
    public List<CommentResponse> list(
            final @Nullable @LoginMemberId(required = false) Long loginMemberId,
            final @PathVariable Long boardId) {
        
        return commentService.getCommentList(loginMemberId, boardId);
    }
    
    @PostMapping("/boards/{boardId}/comments")
    public ResponseEntity<?> write(final @LoginMemberId Long loginMemberId,
                                   final @PathVariable Long boardId,
                                   final @Valid @RequestBody WriteCommentRequest request,
                                   final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                                 .body(Map.of("message",
                                              getErrorMessage(bindingResult, "content")));
        }
        
        commentService.writeComment(loginMemberId, boardId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                             .build();
    }
    
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<?> update(final @LoginMemberId Long loginMemberId,
                                    final @PathVariable Long commentId,
                                    final @Valid @RequestBody UpdateCommentRequest request,
                                    final BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                                 .body(Map.of("message",
                                              getErrorMessage(bindingResult, "content")));
        }
        
        commentService.updateComment(loginMemberId,
                                     commentId,
                                     request);
        
        return ResponseEntity.noContent()
                             .build();
    }
    
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(final @LoginMemberId Long loginMemberId,
                       final @PathVariable Long commentId) {
        commentService.deleteComment(loginMemberId, commentId);
    }
    
    private static String getErrorMessage(final BindingResult bindingResult, final String fieldName) {
        assert (fieldName != null);
        assert (bindingResult != null);
        
        final String errorMessage = "입력값이 올바르지 않습니다.";
        
        if (bindingResult.hasFieldErrors(fieldName)) {
            final String defaultMessage = bindingResult.getFieldError(fieldName)
                                                       .getDefaultMessage();
            return StringUtils.hasText(defaultMessage)
                   ? defaultMessage
                   : errorMessage;
        }
        
        return errorMessage;
    }
}
