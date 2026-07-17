package com.minseok.devboard.comment.entity;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.global.entity.BaseTimeEntity;
import com.minseok.devboard.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.minseok.devboard.comment.entity.CommentStatus.ACTIVE;
import static com.minseok.devboard.comment.entity.CommentStatus.DELETED;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

@Entity @Table(name = "comment")
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Comment extends BaseTimeEntity {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "comment_id", nullable = false)
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;
    
    /**
     * (parent == null) -> 최상위 댓글
     * <p>
     * (parent != null) -> 대댓글
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(nullable = false)
    @Enumerated(STRING)
    private CommentStatus status;
    
    //==생성 메서드==//
    
    /**
     * 최상위 댓글 생성
     */
    public static Comment create(final Member member,
                                 final Board board,
                                 final String content) {
        validateMemberStatus(member);
        validateBoardStatus(board);
        
        final Comment comment = new Comment();
        
        // 댓글 생성 시 초기값
        comment.status = ACTIVE;
        comment.parent = null;
        
        comment.member = member;
        comment.board = board;
        comment.update(content);
        
        return comment;
    }
    
    /**
     * 대댓글 생성
     */
    public static Comment createReply(final Member member,
                                      final Board board,
                                      final Comment parent,
                                      final String content) {
        requireNonNull(parent);
        if (!parent.canReply()) {
            throw new IllegalStateException("대댓글에는 대댓글을 작성할 수 없습니다.");
        }
        
        final Comment comment = create(member, board, content);
        comment.parent = parent;
        
        return comment;
    }
    
    //==비즈니스 로직==//
    
    /**
     * 댓글 수정
     */
    public void update(final String content) {
        validateActiveStatus();
        validateNotBlankText("content", content);
        
        this.content = content;
    }
    
    public void delete() {
        validateActiveStatus();
        status = DELETED;
    }
    
    //==조회 메서드==//
    
    public boolean hasParent() {
        return parent != null;
    }
    
    public boolean canReply() {
        return !hasParent();
    }
    
    public boolean isDeleted() {
        return status == DELETED;
    }
    
    public boolean isWrittenBy(final Long memberId) {
        assert (memberId != null);
        return member.getId().equals(memberId);
    }
    
    public boolean isWrittenIn(final Long boardId) {
        assert (boardId != null);
        return board.getId().equals(boardId);
    }
    
    //==검증 메서드==//
    
    private void validateActiveStatus() {
        if (status != ACTIVE) {
            throw new IllegalStateException("삭제된 댓글입니다.");
        }
    }
    
    private static void validateMemberStatus(final Member member) {
        requireNonNull(member);
        
        if (member.isWithdrawn()) {
            throw new IllegalStateException("탈퇴한 회원은 댓글을 작성할 수 없습니다.");
        }
    }
    
    private static void validateBoardStatus(final Board board) {
        requireNonNull(board);
        
        if (board.isDeleted()) {
            throw new IllegalStateException("삭제된 게시글에는 댓글을 작성할 수 없습니다.");
        }
    }
}
