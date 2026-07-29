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
    
    @JsonProperty("canEdit")
    @Accessors(fluent = true)
    private final boolean canEdit;
    
    @JsonProperty("canDelete")
    @Accessors(fluent = true)
    private final boolean canDelete;
    
    public static CommentResponse toResponse(final Comment comment,
                                             final boolean canEdit,
                                             final boolean canDelete) {
        assert (comment != null);
        
        final String displayContent = comment.isDeleted()
                                      ? "삭제된 댓글입니다."
                                      : comment.getContent();
        
        return new CommentResponse(comment.getId(),
                                   comment.getMember().getNickname(),
                                   displayContent,
                                   comment.getCreatedAt(),
                                   comment.isDeleted(),
                                   comment.hasParent(),
                                   canEdit,
                                   canDelete);
    }
}
