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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    @ResponseStatus(HttpStatus.CREATED)
    public void write(final @LoginMemberId Long loginMemberId,
                      final @PathVariable Long boardId,
                      final @Valid @RequestBody WriteCommentRequest request) {
        commentService.writeComment(loginMemberId, boardId, request);
    }
    
    @PatchMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(final @LoginMemberId Long loginMemberId,
                       final @PathVariable Long commentId,
                       final @Valid @RequestBody UpdateCommentRequest request) {
        commentService.updateComment(loginMemberId,
                                     commentId,
                                     request);
    }
    
    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(final @LoginMemberId Long loginMemberId,
                       final @PathVariable Long commentId) {
        commentService.deleteComment(loginMemberId, commentId);
    }
}
