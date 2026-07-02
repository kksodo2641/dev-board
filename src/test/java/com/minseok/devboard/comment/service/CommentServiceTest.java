package com.minseok.devboard.comment.service;

import com.minseok.devboard.IntegrationTest;
import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.board.repository.BoardRepository;
import com.minseok.devboard.board.service.BoardService;
import com.minseok.devboard.comment.dto.request.WriteCommentRequest;
import com.minseok.devboard.comment.entity.Comment;
import com.minseok.devboard.comment.exception.CommentNotFoundException;
import com.minseok.devboard.comment.exception.ReplyNotAllowedException;
import com.minseok.devboard.comment.repository.CommentRepository;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import com.minseok.devboard.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.minseok.devboard.board.entity.BoardCategory.FREE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RequiredArgsConstructor
class CommentServiceTest extends IntegrationTest {
    
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    
    @Autowired BoardService boardService;
    @Autowired BoardRepository boardRepository;
    
    @Autowired CommentService commentService;
    @Autowired CommentRepository commentRepository;
    
    //==댓글 작성==//
    
    @Test
    @DisplayName("ACTIVE 회원은 댓글을 작성할 수 있다.")
    void writeCommentByActiveMember() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        // when
        final Long commentId = commentService.writeComment(memberId,
                                                           boardId,
                                                           new WriteCommentRequest("comment",
                                                                                   null));
        
        // then
        final Comment comment = commentRepository.findById(commentId)
                                                 .orElseThrow();
        
        assertThat(comment.getContent()).isEqualTo("comment");
        assertThat(comment.getParent()).isNull();
    }
    
    @Test
    @DisplayName("ACTIVE 회원은 대댓글을 작성할 수 있다.")
    void writeReplyByActiveMember() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        final Long commentId = commentService.writeComment(memberId,
                                                           boardId,
                                                           new WriteCommentRequest("comment",
                                                                                   null));
        
        final Comment comment = commentRepository.findById(commentId)
                                                 .orElseThrow();
        assertThat(comment.canReply()).isTrue();
        
        // when
        final Long replyId = commentService.writeComment(memberId,
                                                         boardId,
                                                         new WriteCommentRequest("reply",
                                                                                 commentId));
        
        // then
        final Comment reply = commentRepository.findById(replyId)
                                               .orElseThrow();
        
        assertThat(reply.getContent()).isEqualTo("reply");
        assertThat(reply.getParent()).isEqualTo(comment);
    }
    
    @Test
    @DisplayName("존재하지 않는 회원은 댓글을 작성할 수 없다.")
    void writeCommentByNotFoundMember() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        final Long invalidMemberId = Long.MAX_VALUE;
        
        // when, then
        assertThatThrownBy(() -> commentService.writeComment(invalidMemberId,
                                                             boardId,
                                                             new WriteCommentRequest("comment",
                                                                                     null)))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("탈퇴 회원은 댓글을 작성할 수 없다.")
    void writeCommentByWithdrawnMember() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        memberService.withdraw(memberId);
        final Member member = memberRepository.findById(memberId)
                                              .orElseThrow();
        assertThat(member.isWithdrawn()).isTrue();
        
        // when, then
        assertThatThrownBy(() -> commentService.writeComment(memberId,
                                                             boardId,
                                                             new WriteCommentRequest("comment",
                                                                                     null)))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글에는 댓글을 작성할 수 없다.")
    void writeCommentToNotFoundBoard() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long invalidBoardId = Long.MAX_VALUE;
        
        // when, then
        assertThatThrownBy(() -> commentService.writeComment(memberId,
                                                             invalidBoardId,
                                                             new WriteCommentRequest("comment",
                                                                                     null)))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("삭제된 게시글에는 댓글을 작성할 수 없다.")
    void writeCommentToDeletedBoard() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        boardService.deleteBoard(memberId, boardId);
        
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        assertThat(board.isDeleted()).isTrue();
        
        // when, then
        assertThatThrownBy(() -> commentService.writeComment(memberId,
                                                             boardId,
                                                             new WriteCommentRequest("comment",
                                                                                     null)))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    @Test
    @DisplayName("존재하지 않는 댓글에는 대댓글을 작성할 수 없다.")
    void writeReplyToNotFoundComment() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        final Long invalidParentId = Long.MAX_VALUE;
        
        // when, then
        assertThatThrownBy(() -> commentService.writeComment(memberId,
                                                             boardId,
                                                             new WriteCommentRequest("reply",
                                                                                     invalidParentId)))
                .isInstanceOf(CommentNotFoundException.class);
    }
    
    @Test
    @DisplayName("대댓글에는 대댓글을 작성할 수 없다.")
    void writeReplyToReply() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        final Long commentId = commentService.writeComment(memberId,
                                                           boardId,
                                                           new WriteCommentRequest("comment",
                                                                                   null));
        final Long replyId = commentService.writeComment(memberId,
                                                         boardId,
                                                         new WriteCommentRequest("reply1",
                                                                                 commentId));
        
        final Comment reply = commentRepository.findById(replyId)
                                               .orElseThrow();
        
        // when
        assertThatThrownBy(() -> commentService.writeComment(memberId,
                                                             boardId,
                                                             new WriteCommentRequest("reply2",
                                                                                     replyId)))
                .isInstanceOf(ReplyNotAllowedException.class);
    }
    
    @Test
    @DisplayName("삭제된 댓글에도 대댓글을 작성할 수 있다.")
    void writeReplyToDeletedComment() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        final Long commentId = commentService.writeComment(memberId,
                                                           boardId,
                                                           new WriteCommentRequest("comment",
                                                                                   null));
        final Comment comment = commentRepository.findById(commentId)
                                                 .orElseThrow();
        comment.delete();
        assertThat(comment.isDeleted()).isTrue();
        
        // when
        final Long replyId = commentService.writeComment(memberId,
                                                         boardId,
                                                         new WriteCommentRequest("replyToDeletedComment",
                                                                                 commentId));
        
        // then
        final Comment reply = commentRepository.findById(replyId)
                                               .orElseThrow();
        
        assertThat(reply.getContent()).isEqualTo("replyToDeletedComment");
        assertThat(reply.getParent()).isEqualTo(comment);
    }
    
    private Long createMember(final String email, final String nickname) {
        return memberService.signup(new SignupRequest(email,
                                                      "password123!",
                                                      nickname,
                                                      Gender.NONE));
    }
    
    private Long createBoard(final Long memberId) {
        return boardService.writeBoard(memberId,
                                       new WriteBoardRequest("title", "content", FREE));
    }
}
