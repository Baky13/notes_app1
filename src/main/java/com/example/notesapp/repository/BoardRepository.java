package com.example.notesapp.repository;

import com.example.notesapp.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByUserId(Long userId);
    Optional<Board> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT b FROM Board b LEFT JOIN FETCH b.columns c LEFT JOIN FETCH c.tasks WHERE b.id = :id AND b.user.id = :userId")
    Optional<Board> findByIdAndUserIdWithColumnsAndTasks(@Param("id") Long id, @Param("userId") Long userId);
    
    @Query("SELECT b FROM Board b LEFT JOIN FETCH b.columns LEFT JOIN FETCH b.user WHERE b.id = :id AND b.user.id = :userId")
    Optional<Board> findByIdAndUserIdWithColumns(@Param("id") Long id, @Param("userId") Long userId);
}
