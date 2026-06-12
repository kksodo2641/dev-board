package com.minseok.devboard.board.entity;

import com.minseok.devboard.global.entity.BaseTimeEntity;
import com.minseok.devboard.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PROTECTED;

@Entity @Table(name = "board")
@NoArgsConstructor(access = PROTECTED)
@Getter
public class Board extends BaseTimeEntity {
    
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "board_id", nullable = false)
    private Long id;
    
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member writer;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Lob
    @Column(nullable = false)
    private String content;
    
    @Column(nullable = false)
    @Enumerated(STRING)
    private BoardCategory category;
    
    @Column(nullable = false)
    @Enumerated(STRING)
    private BoardStatus status;
    
    @Column(nullable = false)
    private int viewCount;
    
    //==생성 메서드==//
    public static Board create(final Member writer,
                               final String title,
                               final String content,
                               final BoardCategory category) {
        final Board board = new Board();
        
        requireNonNull(writer);
        board.writer = writer;
        
        board.update(title, content, category);
        
        // 생성 시 고정 값
        board.status = BoardStatus.ACTIVE;
        board.viewCount = 0;
        
        return board;
    }
    
    //==비즈니스 로직==//
    public void update(final String title,
                       final String content,
                       final BoardCategory category) {
        changeTitle(title);
        changeContent(content);
        changeCategory(category);
    }
    
    public void increaseViewCount() {
        ++viewCount;
    }
    
    public void delete() {
        if (status == BoardStatus.DELETED) {
            throw new IllegalStateException("이미 삭제된 게시글입니다.");
        }
        
        status = BoardStatus.DELETED;
    }
    
    //==내부 상태 변경 메서드==//
    private void changeTitle(final String title) {
        validateNotBlankText("title", title);
        this.title = title;
    }
    
    private void changeContent(final String content) {
        validateNotBlankText("content", content);
        this.content = content;
    }
    
    private void changeCategory(final BoardCategory category) {
        requireNonNull(category);
        this.category = category;
    }
    
    private static void validateNotBlankText(final String fieldName, final String value) {
        assert (fieldName != null);
        
        requireNonNull(value);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은 공백일 수 없습니다.");
        }
    }
}

















