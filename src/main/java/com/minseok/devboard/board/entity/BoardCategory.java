package com.minseok.devboard.board.entity;

import com.minseok.devboard.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum BoardCategory {
    FREE("자유 게시판"),
    QNA("질문/답변"),
    STUDY("스터디"),
    JOB("취업"),
    NOTICE("공지사항");
    
    private final String description;
    
    public static List<BoardCategory> userCategories() {
        return Arrays.stream(values())
                     .filter(category -> category != NOTICE)
                     .toList();
    }
    
    public boolean canWrite(final Role role) {
        assert (role != null);
        
        return (role == Role.ADMIN)
                || (this != NOTICE);
    }
}










