package com.minseok.devboard.board.exception;

public class BoardNotFoundException extends RuntimeException {
    
    public BoardNotFoundException() {
        super("존재하지 않는 게시글입니다.");
    }
}
