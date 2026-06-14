package com.minseok.devboard.board.service;

import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardStatus;
import com.minseok.devboard.board.exception.BoardNotFoundException;
import com.minseok.devboard.board.repository.BoardRepository;
import com.minseok.devboard.global.exception.AccessDeniedException;
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
public class BoardService {
    
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    
    @Transactional
    public Long writeBoard(final Long memberId,
                           final WriteBoardRequest request) {
        assert (memberId != null);
        assert (request != null);
        
        final Member writer = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                                              .orElseThrow(MemberNotFoundException::new);
        
        if (!request.getCategory()
                    .canWrite(writer.getRole())) {
            throw new AccessDeniedException();
        }
        
        final Board board = Board.create(writer,
                                         request.getTitle(),
                                         request.getContent(),
                                         request.getCategory());
        
        return boardRepository.save(board)
                              .getId();
    }
    
    public BoardDetailResponse readBoard(final Long boardId) {
        assert (boardId != null);
        
        return boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                              .map(BoardDetailResponse::toResponse)
                              .orElseThrow(BoardNotFoundException::new);
    }
}

















