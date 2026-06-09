package com.minseok.devboard.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberStatus {
    ACTIVE("활성"),
    DELETED("탈퇴");
    
    private final String description;
}

















