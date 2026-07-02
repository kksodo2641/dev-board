package com.minseok.devboard.comment.service;

import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardStatus;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.board.repository.BoardRepository;
import com.minseok.devboard.comment.dto.request.WriteCommentRequest;
import com.minseok.devboard.comment.entity.Comment;
import com.minseok.devboard.comment.exception.CommentNotFoundException;
import com.minseok.devboard.comment.exception.ReplyNotAllowedException;
import com.minseok.devboard.comment.repository.CommentRepository;
import com.minseok.devboard.member.entity.Member;
import com.minseok.devboard.member.entity.MemberStatus;
import com.minseok.devboard.member.exception.MemberNotFoundException;
import com.minseok.devboard.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @throws CommentNotFoundException 대댓글의 부모 댓글이 존재하지 않는 경우
     * @throws ReplyNotAllowedException 대댓글에 대댓글을 작성하는 경우
     */
    @Transactional
    public Long writeComment(final Long memberId,
                             final Long boardId,
                             final WriteCommentRequest request) {
        
        assert (request != null);
        
        final Member member = findActiveMember(memberId);
        final Board board = findActiveBoard(boardId);
        
        // 댓글 작성
        if (request.getParentId() == null) {
            return commentRepository.save(Comment.create(member, board, request.getContent()))
                                    .getId();
        }
        
        // 대댓글 작성
        final Comment parent = findParentComment(request.getParentId());
        if (!parent.canReply()) {
            throw new ReplyNotAllowedException();
        }
        
        return commentRepository.save(Comment.createReply(member, board, parent, request.getContent()))
                                .getId();
    }
    
    /**
     * 활성 회원 조회
     *
     * @throws MemberNotFoundException 존재하지 않거나 탈퇴 회원인 경우
     */
    private Member findActiveMember(final Long memberId) {
        return memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                               .orElseThrow(MemberNotFoundException::new);
    }
    
    /**
     * 활성 게시글 조회
     *
     * @throws BoardNotFoundException 존재하지 않거나 삭제된 게시글인 경우
     */
    private Board findActiveBoard(final Long boardId) {
        return boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                              .orElseThrow(BoardNotFoundException::new);
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
}
