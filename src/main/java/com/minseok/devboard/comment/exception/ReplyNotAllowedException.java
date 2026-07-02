package com.minseok.devboard.comment.exception;

public class ReplyNotAllowedException extends RuntimeException {
    
    public ReplyNotAllowedException() {
        super("대댓글에는 대댓글을 작성할 수 없습니다.");
    }
}
