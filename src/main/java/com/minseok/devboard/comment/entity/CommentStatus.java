package com.minseok.devboard.comment.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommentStatus {
    
    ACTIVE("활성"),
    DELETED("삭제");
    
    private final String description;
}
