package com.minseok.devboard.comment.service;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardStatus;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.board.repository.BoardRepository;
import com.minseok.devboard.comment.dto.request.UpdateCommentRequest;
import com.minseok.devboard.comment.dto.request.WriteCommentRequest;
import com.minseok.devboard.comment.dto.response.CommentResponse;
import com.minseok.devboard.comment.entity.Comment;
import com.minseok.devboard.comment.entity.CommentStatus;
import com.minseok.devboard.comment.exception.CommentNotFoundException;
import com.minseok.devboard.comment.exception.ReplyNotAllowedException;
import com.minseok.devboard.comment.repository.CommentRepository;
import com.minseok.devboard.global.exception.AccessDeniedException;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minseok.devboard.comment.dto.response.CommentResponse.toResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {
    
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    
    /**
     * 댓글 작성
     *
     * @throws MemberNotFoundException  존재하지 않거나 탈퇴 회원인 경우
     * @throws BoardNotFoundException   존재하지 않거나 삭제된 게시글인 경우
     * @throws CommentNotFoundException 대댓글의 부모 댓글이 존재하지 않거나, 현재 게시글에 속하지 않는 경우
     * @throws ReplyNotAllowedException 대댓글에 대댓글을 작성하는 경우
     */
    @Transactional
    public Long writeComment(final Long memberId,
                             final Long boardId,
                             final WriteCommentRequest request) {
        assert (request != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        final Board board = findActiveBoardElseThrow(boardId);
        
        // 댓글 작성
        if (request.getParentId() == null) {
            return commentRepository.save(Comment.create(member,
                                                         board,
                                                         request.getContent()))
                                    .getId();
        }
        
        // 대댓글 작성
        final Comment parent = findParentComment(request.getParentId());
        
        if (!parent.isWrittenIn(boardId)) {
            throw new CommentNotFoundException();
        }
        
        if (!parent.canReply()) {
            throw new ReplyNotAllowedException();
        }
        
        return commentRepository.save(Comment.createReply(member,
                                                          board,
                                                          parent,
                                                          request.getContent()))
                                .getId();
    }
    
    /**
     * 댓글 목록 조회
     * <p>
     * 삭제된 게시글도 댓글 조회 가능
     *
     * @throws BoardNotFoundException 존재하지 않는 게시글인 경우
     */
    public List<CommentResponse> getCommentList(final @Nullable Long readerId,
                                                final Long boardId) {
        assert (boardId != null);
        
        // 삭제 게시글도 포함해서 조회
        final Board board = boardRepository.findById(boardId)
                                           .orElseThrow(BoardNotFoundException::new);
        
        // 댓글 목록 조회(생성순)
        final List<Comment> commentsByIdAsc = commentRepository.findByBoardOrderByIdAsc(board);
        
        final List<Comment> parentComments = new ArrayList<>();
        final Map<Long, List<Comment>> replyMap = new HashMap<>();
        
        for (final Comment c : commentsByIdAsc) {
            if (!c.hasParent()) { // 최상위 댓글
                parentComments.add(c);
                replyMap.put(c.getId(), new ArrayList<>());
                
            } else { // 대댓글
                replyMap.get(c.getParent().getId())
                        .add(c);
            }
        }
        
        // 회원 조회
        final Member member = readerId == null
                              ? null
                              : memberRepository.findByIdAndStatus(readerId, MemberStatus.ACTIVE)
                                                .orElse(null);
        
        final List<CommentResponse> result = new ArrayList<>();
        
        for (final Comment parent : parentComments) {
            result.add(toResponse(parent,
                                  canEdit(member, board, parent),
                                  canDelete(member, board, parent)));
            
            for (final Comment reply : replyMap.get(parent.getId())) {
                result.add(toResponse(reply,
                                      canEdit(member, board, reply),
                                      canDelete(member, board, reply)));
            }
        }
        
        return result;
    }
    
    /**
     * 댓글 수정
     *
     * @throws MemberNotFoundException  존재하지 않거나 탈퇴 회원인 경우
     * @throws CommentNotFoundException 존재하지 않거나 삭제된 댓글인 경우
     * @throws BoardNotFoundException   삭제된 게시글에 속한 댓글인 경우
     * @throws AccessDeniedException    수정 권한이 없는 경우
     */
    @Transactional
    public void updateComment(final Long memberId,
                              final Long commentId,
                              final UpdateCommentRequest request) {
        assert (memberId != null);
        assert (commentId != null);
        assert (request != null);
        
        final Member member = findActiveMemberElseThrow(memberId);
        final Comment comment = findActiveCommentElseThrow(commentId);
        final Board board = comment.getBoard();
        
        if (board.isDeleted()) {
            throw new BoardNotFoundException();
        }
        
        if (!comment.isWrittenBy(member.getId())) {
            throw new AccessDeniedException();
        }
        
        comment.update(request.getContent());
    }
    
    //==내부 메서드==//
    
    /**
     * 활성 회원 조회
     *
     * @throws MemberNotFoundException 존재하지 않거나 탈퇴 회원인 경우
     */
    private Member findActiveMemberElseThrow(final Long memberId) {
        assert (memberId != null);
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                               .orElseThrow(MemberNotFoundException::new);
    }
    
    /**
     * 활성 게시글 조회
     *
     * @throws BoardNotFoundException 존재하지 않거나 삭제된 게시글인 경우
     */
    private Board findActiveBoardElseThrow(final Long boardId) {
        assert (boardId != null);
        return boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                              .orElseThrow(BoardNotFoundException::new);
    }
    
    /**
     * 활성 댓글 조회
     *
     * @throws CommentNotFoundException 존재하지 않거나 삭제된 댓글인 경우
     */
    private Comment findActiveCommentElseThrow(final Long commentId) {
        assert (commentId != null);
        return commentRepository.findByIdAndStatus(commentId, CommentStatus.ACTIVE)
                                .orElseThrow(CommentNotFoundException::new);
    }
    
    /**
     * 부모 댓글 조회
     *
     * @throws CommentNotFoundException 부모 댓글이 존재하지 않는 경우
     */
    private Comment findParentComment(final Long parentId) {
        assert (parentId != null);
        return commentRepository.findById(parentId)
                                .orElseThrow(CommentNotFoundException::new);
    }
    
    private boolean canEdit(final @Nullable Member member,
                            final Board board,
                            final Comment comment) {
        assert (board != null);
        assert (comment != null);
        
        if (member == null
                || board.isDeleted()
                || comment.isDeleted()) {
            return false;
        }
        
        return comment.isWrittenBy(member.getId());
    }
    
    private boolean canDelete(final @Nullable Member member,
                              final Board board,
                              final Comment comment) {
        assert (board != null);
        assert (comment != null);
        
        if (member == null
                || board.isDeleted()
                || comment.isDeleted()) {
            return false;
        }
        
        return member.isAdmin()
                || comment.isWrittenBy(member.getId());
    }
}
