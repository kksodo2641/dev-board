package com.minseok.devboard.board.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BoardStatus {
    ACTIVE("활성"),
    DELETED("삭제됨");
    
    private final String description;
}
