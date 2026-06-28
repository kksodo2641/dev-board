package com.minseok.devboard.board.repository.paging;

import com.minseok.devboard.board.entity.Board;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class BoardPagingRepositoryImpl implements BoardPagingRepository {
    
    private final EntityManager em;
    
    @Override
    public long countBoards() {
        return em.createQuery("select count(b) from Board b", Long.class)
                 .getSingleResult();
    }
    
    @Override
    public List<Board> findBoardList(final int offset, final int limit) {
        return em.createQuery("select b from Board b order by b.id desc", Board.class)
                 .setFirstResult(offset)
                 .setMaxResults(limit)
                 .getResultList();
    }
}
