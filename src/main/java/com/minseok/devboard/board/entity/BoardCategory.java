package com.minseok.devboard.board.entity;

import com.minseok.devboard.member.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BoardCategory {
    NOTICE("공지사항"),
    FREE("자유 게시판"),
    QNA("질문/답변"),
    STUDY("스터디"),
    JOB("취업");
    
    private final String description;
    
    public boolean canWrite(final Role role) {
        assert (role != null);
        
        return (role == Role.ADMIN)
                || (this != NOTICE);
    }
}










