package com.minseok.devboard.member.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    MALE("남성"),
    FEMALE("여성"),
    NONE("선택안함");
    
    private final String description;
}











