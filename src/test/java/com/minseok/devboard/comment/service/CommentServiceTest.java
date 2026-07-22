package com.minseok.devboard.comment.service;

import com.minseok.devboard.IntegrationTest;
import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardStatus;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.board.repository.BoardRepository;
import com.minseok.devboard.board.service.BoardService;
import com.minseok.devboard.comment.dto.request.UpdateCommentRequest;
import com.minseok.devboard.comment.dto.request.WriteCommentRequest;
import com.minseok.devboard.comment.dto.response.CommentResponse;
import com.minseok.devboard.comment.entity.Comment;
import com.minseok.devboard.comment.entity.CommentStatus;
import com.minseok.devboard.comment.exception.CommentNotFoundException;
import com.minseok.devboard.comment.exception.ReplyNotAllowedException;
import com.minseok.devboard.comment.repository.CommentRepository;
import com.minseok.devboard.global.exception.AccessDeniedException;
import com.minseok.devboard.member.dto.request.SignupRequest;
import com.minseok.devboard.member.entity.Gender;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import com.minseok.devboard.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.minseok.devboard.board.entity.BoardCategory.FREE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

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
        final Comment comment = findCommentElseThrow(commentId);
        
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
        
        final Comment comment = findCommentElseThrow(commentId);
        assertThat(comment.canReply()).isTrue();
        
        // when
        final Long replyId = commentService.writeComment(memberId,
                                                         boardId,
                                                         new WriteCommentRequest("reply",
                                                                                 commentId));
        
        // then
        final Comment reply = findCommentElseThrow(replyId);
        
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
    @DisplayName("다른 게시글의 댓글에는 대댓글을 작성할 수 없다.")
    void writeReplyToAnotherBoardComment() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        
        final Long boardId1 = createBoard(memberId);
        final Long boardId2 = createBoard(memberId);
        
        final Long commentId = createComment(memberId, boardId1, "comment");
        
        // when, then
        assertThatThrownBy(() -> commentService.writeComment(memberId,
                                                             boardId2,
                                                             new WriteCommentRequest("reply",
                                                                                     commentId)))
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
        
        // when
        assertThatThrownBy(
                () -> commentService.writeComment(memberId,
                                                  boardId,
                                                  new WriteCommentRequest("reply2", replyId)))
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
        final Comment comment = findCommentElseThrow(commentId);
        comment.delete();
        assertThat(comment.isDeleted()).isTrue();
        
        // when
        final Long replyId = commentService.writeComment(memberId,
                                                         boardId,
                                                         new WriteCommentRequest("replyToDeletedComment",
                                                                                 commentId));
        
        // then
        final Comment reply = findCommentElseThrow(replyId);
        
        assertThat(reply.getContent()).isEqualTo("replyToDeletedComment");
        assertThat(reply.getParent()).isEqualTo(comment);
    }
    
    //==댓글 조회==//
    
    @Test
    @DisplayName("댓글이 없는 게시글은 빈 목록을 반환한다.")
    void getCommentListWithNoComments() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        // when
        final List<CommentResponse> commentList = commentService.getCommentList(null, boardId);
        
        // then
        assertThat(commentList).isEmpty();
    }
    
    @Test
    @DisplayName("최상위 댓글만 존재하는 게시글을 조회할 수 있다.")
    void getCommentListWithOnlyParentComments() {
        // given
        final Long memberId1 = createMember("test1@example.com", "m1");
        final Long memberId2 = createMember("test2@example.com", "m2");
        final Long memberId3 = createMember("test3@example.com", "m3");
        
        final Long boardId = createBoard(memberId1);
        
        final Long commentId1 = createComment(memberId1, boardId, "c1");
        final Long commentId2 = createComment(memberId2, boardId, "c2");
        final Long commentId3 = createComment(memberId3, boardId, "c3");
        final Long commentId4 = createComment(memberId2, boardId, "c4");
        final Long commentId5 = createComment(memberId1, boardId, "c5");
        final Long commentId6 = createComment(memberId3, boardId, "c6");
        
        // when
        final List<CommentResponse> commentList = commentService.getCommentList(null, boardId);
        
        // then
        assertThat(commentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::getWriterNickname,
                            CommentResponse::getContent,
                            CommentResponse::hasParent)
                .containsExactly(
                        tuple(commentId1, "m1", "c1", false),
                        tuple(commentId2, "m2", "c2", false),
                        tuple(commentId3, "m3", "c3", false),
                        tuple(commentId4, "m2", "c4", false),
                        tuple(commentId5, "m1", "c5", false),
                        tuple(commentId6, "m3", "c6", false)
                );
    }
    
    @Test
    @DisplayName("댓글과 대댓글이 함께 존재하는 게시글을 조회할 수 있다.")
    void getCommentListWithReplies() {
        // given
        final Long m1 = createMember("test1@example.com", "m1");
        final Long m2 = createMember("test2@example.com", "m2");
        final Long m3 = createMember("test3@example.com", "m3");
        
        final Long boardId = createBoard(m1);
        
        final Long c1 = createComment(m1, boardId, "A");
        final Long c2 = createComment(m2, boardId, "B");
        final Long c3 = createComment(m3, boardId, "C");
        
        final Long r1 = createReply(m3, boardId, c1, "A-1");
        final Long r2 = createReply(m2, boardId, c2, "B-1");
        final Long r3 = createReply(m1, boardId, c2, "B-2");
        final Long r4 = createReply(m2, boardId, c1, "A-2");
        final Long r5 = createReply(m1, boardId, c3, "C-1");
        final Long r6 = createReply(m1, boardId, c1, "A-3");
        
        // when
        final List<CommentResponse> commentList = commentService.getCommentList(null, boardId);
        
        // then
        assertThat(commentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::getWriterNickname,
                            CommentResponse::getContent,
                            CommentResponse::hasParent)
                .containsExactly(
                        tuple(c1, "m1", "A", false),
                        tuple(r1, "m3", "A-1", true),
                        tuple(r4, "m2", "A-2", true),
                        tuple(r6, "m1", "A-3", true),
                        
                        tuple(c2, "m2", "B", false),
                        tuple(r2, "m2", "B-1", true),
                        tuple(r3, "m1", "B-2", true),
                        
                        tuple(c3, "m3", "C", false),
                        tuple(r5, "m1", "C-1", true)
                );
    }
    
    @Test
    @DisplayName("삭제된 댓글의 내용은 '삭제된 댓글입니다.' 형태로 표시된다.")
    void getCommentListWithDeletedComments() {
        // given
        final Long m1 = createMember("test1@example.com", "m1");
        final Long m2 = createMember("test2@example.com", "m2");
        final Long m3 = createMember("test3@example.com", "m3");
        
        final Long boardId = createBoard(m1);
        
        final Long c1 = createComment(m1, boardId, "A");
        final Long c2 = createComment(m2, boardId, "B");
        final Long c3 = createComment(m3, boardId, "C");
        
        deleteComment(c2);
        
        final Long r1 = createReply(m3, boardId, c1, "A-1");
        final Long r2 = createReply(m2, boardId, c2, "B-1");
        final Long r3 = createReply(m1, boardId, c2, "B-2");
        final Long r4 = createReply(m2, boardId, c1, "A-2");
        final Long r5 = createReply(m1, boardId, c3, "C-1");
        final Long r6 = createReply(m1, boardId, c1, "A-3");
        
        deleteComment(r1, r3, r5);
        
        // when
        final List<CommentResponse> commentList = commentService.getCommentList(null, boardId);
        
        // then
        assertThat(commentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::getWriterNickname,
                            CommentResponse::getContent,
                            CommentResponse::hasParent)
                .containsExactly(
                        tuple(c1, "m1", "A", false),
                        tuple(r1, "m3", "삭제된 댓글입니다.", true),
                        tuple(r4, "m2", "A-2", true),
                        tuple(r6, "m1", "A-3", true),
                        
                        tuple(c2, "m2", "삭제된 댓글입니다.", false),
                        tuple(r2, "m2", "B-1", true),
                        tuple(r3, "m1", "삭제된 댓글입니다.", true),
                        
                        tuple(c3, "m3", "C", false),
                        tuple(r5, "m1", "삭제된 댓글입니다.", true)
                );
    }
    
    @Test
    @DisplayName("삭제된 게시글도 댓글을 조회할 수 있다.")
    void getCommentListFromDeletedBoard() {
        // given
        final Long m1 = createMember("test1@example.com", "m1");
        final Long m2 = createMember("test2@example.com", "m2");
        final Long m3 = createMember("test3@example.com", "m3");
        
        final Long boardId = createBoard(m1);
        
        final Long c1 = createComment(m1, boardId, "A");
        final Long c2 = createComment(m2, boardId, "B");
        final Long c3 = createComment(m3, boardId, "C");
        
        deleteComment(c2);
        
        final Long r1 = createReply(m3, boardId, c1, "A-1");
        final Long r2 = createReply(m2, boardId, c2, "B-1");
        final Long r3 = createReply(m1, boardId, c2, "B-2");
        final Long r4 = createReply(m2, boardId, c1, "A-2");
        final Long r5 = createReply(m1, boardId, c3, "C-1");
        final Long r6 = createReply(m1, boardId, c1, "A-3");
        
        deleteComment(r1, r3, r5);
        
        boardService.deleteBoard(m1, boardId);
        
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow();
        assertThat(board.isDeleted()).isTrue();
        
        // when
        final List<CommentResponse> commentList = commentService.getCommentList(null, boardId);
        
        // then
        assertThat(commentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::getWriterNickname,
                            CommentResponse::getContent,
                            CommentResponse::hasParent)
                .containsExactly(
                        tuple(c1, "m1", "A", false),
                        tuple(r1, "m3", "삭제된 댓글입니다.", true),
                        tuple(r4, "m2", "A-2", true),
                        tuple(r6, "m1", "A-3", true),
                        
                        tuple(c2, "m2", "삭제된 댓글입니다.", false),
                        tuple(r2, "m2", "B-1", true),
                        tuple(r3, "m1", "삭제된 댓글입니다.", true),
                        
                        tuple(c3, "m3", "C", false),
                        tuple(r5, "m1", "삭제된 댓글입니다.", true)
                );
    }
    
    @Test
    @DisplayName("존재하지 않는 게시글은 댓글을 조회할 수 없다.")
    void getCommentListFromNotFoundBoard() {
        // given, when, then
        final Long invalidBoardId = Long.MAX_VALUE;
        
        assertThatThrownBy(() -> commentService.getCommentList(null, invalidBoardId))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    //==댓글 수정/삭제 권한==//
    
    @Test
    @DisplayName("비회원은 댓글 수정/삭제 권한이 없다.")
    void getCommentListPermissionsForGuest() {
        // given
        final Long memberId = createMember("test@example.com", "tester");
        final Long boardId = createBoard(memberId);
        
        final Long commentId = createComment(memberId, boardId, "comment");
        final Long replyId = createReply(memberId, boardId, commentId, "reply");
        
        // when
        final List<CommentResponse> guestCommentList = commentService.getCommentList(null,
                                                                                     boardId);
        
        // then
        assertThat(guestCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, false, false),
                        tuple(replyId, false, false));
    }
    
    @Test
    @DisplayName("댓글 작성자는 본인 댓글의 수정/삭제 권한이 있다.")
    void getCommentListPermissionsForWriter() {
        // given
        final Long boardWriterId = createMember("boardWriter@example.com", "boardWriter");
        final Long commentWriterId = createMember("commentWriter@example.com", "commentWriter");
        
        final Long boardId = createBoard(boardWriterId);
        
        final Long commentId = createComment(commentWriterId, boardId, "comment");
        final Long replyId = createReply(commentWriterId, boardId, commentId, "reply");
        
        // when
        final List<CommentResponse> writerCommentList = commentService.getCommentList(commentWriterId,
                                                                                      boardId);
        
        // then
        assertThat(writerCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, true, true),
                        tuple(replyId, true, true));
    }
    
    @Test
    @DisplayName("댓글 작성자가 아닌 일반 회원은 댓글 수정/삭제 권한이 없다.")
    void getCommentListPermissionsForNonWriterMember() {
        // given
        final Long boardWriterId = createMember("boardWriter@example.com", "boardWriter");
        final Long commentWriterId = createMember("commentWriter@example.com", "commentWriter");
        final Long nonWriterId = createMember("nonWriter@example.com", "nonWriter");
        
        final Long boardId = createBoard(boardWriterId);
        
        final Long commentId = createComment(commentWriterId, boardId, "comment");
        final Long replyId = createReply(commentWriterId, boardId, commentId, "reply");
        
        // when
        final List<CommentResponse> nonWriterCommentList = commentService.getCommentList(nonWriterId,
                                                                                         boardId);
        
        final List<CommentResponse> boardWriterCommentList = commentService.getCommentList(boardWriterId,
                                                                                           boardId);
        
        // then
        assertThat(nonWriterCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, false, false),
                        tuple(replyId, false, false));
        
        assertThat(boardWriterCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, false, false),
                        tuple(replyId, false, false));
    }
    
    @Test
    @DisplayName("관리자는 본인 댓글의 수정/삭제 권한이 있고, 다른 회원이 작성한 댓글은 삭제 권한만 있다.")
    void getCommentListPermissionsForAdminMember() {
        // given
        final Long adminId = getAdminMemberId();
        final Long memberId = createMember("normalMember@example.com", "normalMember");
        
        final Long boardId = createBoard(adminId);
        
        final Long adminCommentId = createComment(adminId, boardId, "adminComment");
        final Long memberCommentId = createComment(memberId, boardId, "memberComment");
        
        final Long adminReplyId = createReply(adminId, boardId, memberCommentId, "adminReply");
        final Long memberReplyId = createReply(memberId, boardId, adminCommentId, "memberReply");
        
        // when
        final List<CommentResponse> adminCommentList = commentService.getCommentList(adminId,
                                                                                     boardId);
        
        // then
        assertThat(adminCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(adminCommentId, true, true),
                        tuple(memberReplyId, false, true),
                        tuple(memberCommentId, false, true),
                        tuple(adminReplyId, true, true));
    }
    
    @Test
    @DisplayName("존재하지 않는 회원은 댓글 수정/삭제 권한이 없다.")
    void getCommentListPermissionsForNotFoundMember() {
        // given
        final Long boardWriterId = createMember("boardWriter@test.com", "boardWriter");
        final Long commentWriterId = createMember("commentWriter@test.com", "commentWriter");
        
        final Long boardId = createBoard(boardWriterId);
        
        final Long commentId = createComment(commentWriterId, boardId, "comment");
        final Long replyId = createReply(commentWriterId, boardId, commentId, "reply");
        
        // when
        final List<CommentResponse> notFoundMemberCommentList = commentService.getCommentList(Long.MAX_VALUE,
                                                                                              boardId);
        
        // then
        assertThat(notFoundMemberCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, false, false),
                        tuple(replyId, false, false));
    }
    
    @Test
    @DisplayName("탈퇴 회원은 댓글 수정/삭제 권한이 없다.")
    void getCommentListPermissionsForWithdrawnMember() {
        // given
        final Long boardWriterId = createMember("boardWriter@test.com", "boardWriter");
        final Long commentWriterId = createMember("commentWriter@test.com", "commentWriter");
        
        final Long boardId = createBoard(boardWriterId);
        
        final Long commentId = createComment(commentWriterId, boardId, "comment");
        final Long replyId = createReply(commentWriterId, boardId, commentId, "reply");
        
        // 회원 탈퇴
        memberService.withdraw(commentWriterId);
        final Member member = memberRepository.findByIdAndStatus(commentWriterId, MemberStatus.DELETED)
                                              .orElseThrow();
        assertThat(member.isWithdrawn()).isTrue();
        
        // when
        final List<CommentResponse> writerCommentList = commentService.getCommentList(commentWriterId,
                                                                                      boardId);
        
        // then
        assertThat(writerCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, false, false),
                        tuple(replyId, false, false));
    }
    
    @Test
    @DisplayName("삭제된 댓글에는 수정/삭제 권한이 없다.")
    void getCommentListPermissionsWhenCommentIsDeleted() {
        // given
        final Long adminId = getAdminMemberId();
        final Long boardWriterId = createMember("boardWriter@test.com", "boardWriter");
        final Long commentWriterId = createMember("commentWriter@test.com", "commentWriter");
        
        final Long boardId = createBoard(boardWriterId);
        
        final Long commentId = createComment(commentWriterId, boardId, "comment");
        final Long replyId = createReply(commentWriterId, boardId, commentId, "reply");
        
        // 댓글/대댓글 삭제
        deleteComment(commentId);
        deleteComment(replyId);
        
        final Comment comment = commentRepository.findByIdAndStatus(commentId, CommentStatus.DELETED)
                                                 .orElseThrow();
        final Comment reply = commentRepository.findByIdAndStatus(replyId, CommentStatus.DELETED)
                                               .orElseThrow();
        assertThat(comment.isDeleted()).isTrue();
        assertThat(reply.isDeleted()).isTrue();
        
        // when
        final List<CommentResponse> writerCommentList = commentService.getCommentList(commentWriterId,
                                                                                      boardId);
        
        final List<CommentResponse> adminCommentList = commentService.getCommentList(adminId,
                                                                                     boardId);
        
        // then
        assertThat(writerCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::isDeleted,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete
                )
                .containsExactly(
                        tuple(commentId, true, false, false),
                        tuple(replyId, true, false, false));
        
        assertThat(adminCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::isDeleted,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, true, false, false),
                        tuple(replyId, true, false, false));
    }
    
    @Test
    @DisplayName("삭제된 부모 댓글의 활성 대댓글에는 수정/삭제 권한이 있다.")
    void getCommentListPermissionsWhenParentCommentIsDeleted() {
        // given
        final Long boardWriterId = createMember("boardWriter@test.com", "boardWriter");
        final Long commentWriterId = createMember("commentWriter@test.com", "commentWriter");
        
        final Long boardId = createBoard(boardWriterId);
        
        final Long commentId = createComment(commentWriterId, boardId, "comment");
        final Long replyId = createReply(commentWriterId, boardId, commentId, "reply");
        
        // 댓글 삭제
        deleteComment(commentId);
        final Comment comment = commentRepository.findByIdAndStatus(commentId, CommentStatus.DELETED)
                                                 .orElseThrow();
        assertThat(comment.isDeleted()).isTrue();
        
        // when
        final List<CommentResponse> writerCommentList = commentService.getCommentList(commentWriterId,
                                                                                      boardId);
        
        // then
        assertThat(writerCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::isDeleted,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, true, false, false),
                        tuple(replyId, false, true, true));
    }
    
    @Test
    @DisplayName("삭제된 게시글의 댓글에는 수정/삭제 권한이 없다.")
    void getCommentListPermissionsWhenBoardIsDeleted() {
        // given
        final Long adminId = getAdminMemberId();
        final Long boardWriterId = createMember("boardWriter@test.com", "boardWriter");
        final Long commentWriterId = createMember("commentWriter@test.com", "commentWriter");
        
        final Long boardId = createBoard(boardWriterId);
        
        final Long commentId = createComment(commentWriterId, boardId, "comment");
        final Long replyId = createReply(commentWriterId, boardId, commentId, "reply");
        
        // 게시글 삭제
        boardService.deleteBoard(boardWriterId, boardId);
        final Board board = boardRepository.findByIdAndStatus(boardId, BoardStatus.DELETED)
                                           .orElseThrow();
        assertThat(board.isDeleted()).isTrue();
        
        // when
        final List<CommentResponse> writerCommentList = commentService.getCommentList(commentWriterId,
                                                                                      boardId);
        
        final List<CommentResponse> adminCommentList = commentService.getCommentList(adminId,
                                                                                     boardId);
        
        // then
        assertThat(writerCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::isDeleted,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, false, false, false),
                        tuple(replyId, false, false, false));
        
        assertThat(adminCommentList)
                .extracting(CommentResponse::getCommentId,
                            CommentResponse::isDeleted,
                            CommentResponse::canEdit,
                            CommentResponse::canDelete)
                .containsExactly(
                        tuple(commentId, false, false, false),
                        tuple(replyId, false, false, false));
    }
    
    //==댓글 수정==//
    
    @Test
    @DisplayName("작성자는 댓글을 수정할 수 있다.")
    void updateCommentByWriter() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        final Long commentId = createComment(memberId, boardId, "content");
        
        // when
        final String newContent = "new_content";
        commentService.updateComment(memberId,
                                     commentId,
                                     new UpdateCommentRequest(newContent));
        
        // then
        final Comment comment = findCommentElseThrow(commentId);
        
        assertThat(comment.getContent()).isEqualTo(newContent);
        assertThat(comment.isDeleted()).isFalse();
    }
    
    @Test
    @DisplayName("관리자는 본인 댓글을 수정할 수 있다.")
    void updateOwnCommentByAdminMember() {
        // given
        final Long adminId = getAdminMemberId();
        final Long boardId = createBoard(adminId);
        final Long commentId = createComment(adminId, boardId, "content");
        
        // when
        final String newContent = "new_content";
        commentService.updateComment(adminId,
                                     commentId,
                                     new UpdateCommentRequest(newContent));
        
        // then
        final Comment comment = findCommentElseThrow(commentId);
        
        assertThat(comment.getContent()).isEqualTo(newContent);
    }
    
    @Test
    @DisplayName("작성자는 대댓글을 수정할 수 있다.")
    void updateReplyByWriter() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        final Long parentId = createComment(memberId, boardId, "parent");
        final Long replyId = createReply(memberId, boardId, parentId, "reply");
        
        // when
        final String newContent = "new_reply";
        commentService.updateComment(memberId,
                                     replyId,
                                     new UpdateCommentRequest(newContent));
        
        // then
        final Comment reply = findCommentElseThrow(replyId);
        
        assertThat(reply.getContent()).isEqualTo(newContent);
    }
    
    @Test
    @DisplayName("작성자가 아닌 회원은 댓글을 수정할 수 없다.")
    void updateCommentByNotWriter() {
        // given
        final Long writerId = createMember("writer@example.com", "writer");
        final Long notWriterId = createMember("notWriter@example.com", "notWriter");
        final Long boardId = createBoard(writerId);
        final Long commentId = createComment(writerId, boardId, "content");
        
        // when, then
        assertThatThrownBy(
                () -> commentService.updateComment(notWriterId,
                                                   commentId,
                                                   new UpdateCommentRequest("new_content")))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("관리자도 다른 회원의 댓글은 수정할 수 없다.")
    void updateOtherMemberCommentByAdminMember() {
        // given
        final Long writerId = createMember("writer@example.com", "writer");
        final Long adminId = getAdminMemberId();
        
        final Long boardId = createBoard(adminId);
        final Long commentId = createComment(writerId, boardId, "content");
        
        // when, then
        assertThatThrownBy(
                () -> commentService.updateComment(adminId,
                                                   commentId,
                                                   new UpdateCommentRequest("new_content")))
                .isInstanceOf(AccessDeniedException.class);
    }
    
    @Test
    @DisplayName("존재하지 않는 회원은 댓글을 수정할 수 없다.")
    void updateCommentByNotFoundMember() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        final Long commentId = createComment(memberId, boardId, "content");
        
        // when, then
        final Long invalidMemberId = Long.MAX_VALUE;
        assertThatThrownBy(
                () -> commentService.updateComment(invalidMemberId,
                                                   commentId,
                                                   new UpdateCommentRequest("new_content")))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("탈퇴 회원은 댓글을 수정할 수 없다.")
    void updateCommentByWithdrawnMember() {
        // given
        final Long writerId = createMember("test@example.com", "writer");
        final Long boardId = createBoard(writerId);
        final Long commentId = createComment(writerId, boardId, "content");
        
        memberService.withdraw(writerId);
        final Member withdrawnMember = memberRepository.findByIdAndStatus(writerId,
                                                                          MemberStatus.DELETED)
                                                       .orElseThrow();
        assertThat(withdrawnMember.isWithdrawn()).isTrue();
        
        // when, then
        assertThatThrownBy(
                () -> commentService.updateComment(writerId,
                                                   commentId,
                                                   new UpdateCommentRequest("new_content")))
                .isInstanceOf(MemberNotFoundException.class);
    }
    
    @Test
    @DisplayName("존재하지 않는 댓글은 수정할 수 없다.")
    void updateCommentWithNotFoundComment() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        
        // when, then
        final Long invalidCommentId = Long.MAX_VALUE;
        assertThatThrownBy(
                () -> commentService.updateComment(memberId,
                                                   invalidCommentId,
                                                   new UpdateCommentRequest("new_content")))
                .isInstanceOf(CommentNotFoundException.class);
    }
    
    @Test
    @DisplayName("삭제된 댓글은 수정할 수 없다.")
    void updateCommentWithDeletedComment() {
        // given
        final Long writerId = createMember("test@example.com", "writer");
        final Long boardId = createBoard(writerId);
        final Long commentId = createComment(writerId, boardId, "content");
        
        deleteComment(commentId);
        
        final Comment comment = findCommentElseThrow(commentId);
        assertThat(comment.isDeleted()).isTrue();
        
        // when, then
        assertThatThrownBy(
                () -> commentService.updateComment(writerId,
                                                   commentId,
                                                   new UpdateCommentRequest("new_content")))
                .isInstanceOf(CommentNotFoundException.class);
    }
    
    @Test
    @DisplayName("삭제된 댓글의 대댓글은 수정할 수 있다.")
    void updateReplyWithDeletedParentComment() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        final Long parentId = createComment(memberId, boardId, "parent");
        final Long replyId = createReply(memberId, boardId, parentId, "reply");
        
        deleteComment(parentId);
        
        final Comment parent = findCommentElseThrow(parentId);
        assertThat(parent.isDeleted()).isTrue();
        
        // when
        final String newReply = "new_reply";
        commentService.updateComment(memberId,
                                     replyId,
                                     new UpdateCommentRequest(newReply));
        
        // then
        final Comment reply = findCommentElseThrow(replyId);
        
        assertThat(reply.getContent()).isEqualTo(newReply);
        assertThat(reply.getParent()).isEqualTo(parent);
    }
    
    @Test
    @DisplayName("삭제된 게시글의 댓글은 수정할 수 없다.")
    void updateCommentInDeletedBoard() {
        // given
        final Long memberId = createMember("test@example.com", "nickname");
        final Long boardId = createBoard(memberId);
        
        final Long parentId = createComment(memberId, boardId, "parent");
        final Long replyId = createReply(memberId, boardId, parentId, "reply");
        
        boardService.deleteBoard(memberId, boardId);
        
        // when, then
        assertThatThrownBy(
                () -> commentService.updateComment(memberId,
                                                   parentId,
                                                   new UpdateCommentRequest("new_parent")))
                .isInstanceOf(BoardNotFoundException.class);
        
        assertThatThrownBy(
                () -> commentService.updateComment(memberId,
                                                   replyId,
                                                   new UpdateCommentRequest("new_reply")))
                .isInstanceOf(BoardNotFoundException.class);
    }
    
    //==편의 메서드==//
    
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
    
    private Long createComment(final Long memberId,
                               final Long boardId,
                               final String content) {
        return commentService.writeComment(memberId,
                                           boardId,
                                           new WriteCommentRequest(content, null));
    }
    
    private Long createReply(final Long memberId,
                             final Long boardId,
                             final Long parentId,
                             final String content) {
        return commentService.writeComment(memberId,
                                           boardId,
                                           new WriteCommentRequest(content, parentId));
    }
    
    private void deleteComment(final Long... commentIds) {
        for (final Long id : commentIds) {
            final Comment comment = findCommentElseThrow(id);
            comment.delete();
        }
    }
    
    private Comment findCommentElseThrow(final Long commentId) {
        return commentRepository.findById(commentId)
                                .orElseThrow();
    }
}
