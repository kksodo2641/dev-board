package com.minseok.devboard.comment.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.minseok.devboard.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PRIVATE;

@Getter
@AllArgsConstructor(access = PRIVATE)
public class CommentResponse {
    
    private final Long commentId;
    private final String writerNickname;
    private final String content;
    private final LocalDateTime createdAt;
    
    private final boolean deleted;
    
    @JsonProperty("hasParent")
    @Accessors(fluent = true)
    private final boolean hasParent;
    
    public static CommentResponse toResponse(final Comment comment) {
        assert (comment != null);
        
        final String displayContent = comment.isDeleted()
                                      ? "삭제된 댓글입니다."
                                      : comment.getContent();
        
        return new CommentResponse(comment.getId(),
                                   comment.getMember().getNickname(),
                                   displayContent,
                                   comment.getCreatedAt(),
                                   comment.isDeleted(),
                                   comment.hasParent());
    }
}
