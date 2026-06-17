package com.minseok.devboard.board.service;

import com.minseok.devboard.board.dto.request.WriteBoardRequest;
import com.minseok.devboard.board.dto.response.BoardDetailResponse;
import com.minseok.devboard.board.dto.response.BoardListResponse;
import com.minseok.devboard.board.entity.Board;
import com.minseok.devboard.board.entity.BoardCategory;
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

import java.util.List;

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
    
    /**
     * 상세 조회
     */
    public BoardDetailResponse readBoard(final Long boardId) {
        assert (boardId != null);
        
        return boardRepository.findByIdAndStatus(boardId, BoardStatus.ACTIVE)
                              .map(BoardDetailResponse::toResponse)
                              .orElseThrow(BoardNotFoundException::new);
    }
    
    /**
     * 목록 조회
     * 향후, 페이징 추가
     */
    public List<BoardListResponse> getBoardList() {
        return boardRepository.findAllByOrderByIdDesc().stream()
                              .map(BoardListResponse::toResponse)
                              .toList();
    }
    
    public List<BoardCategory> getWritableCategories(final Long memberId) {
        assert (memberId != null);
        
        final Member member = memberRepository.findByIdAndStatus(memberId, MemberStatus.ACTIVE)
                                              .orElseThrow(MemberNotFoundException::new);
        
        return member.isAdmin()
               ? List.of(BoardCategory.values())
               : BoardCategory.userCategories();
    }
}

















