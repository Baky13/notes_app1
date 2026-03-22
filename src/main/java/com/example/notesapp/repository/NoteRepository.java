package com.example.notesapp.repository;

import com.example.notesapp.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByUserId(Long userId, Pageable pageable);
    Optional<Note> findByIdAndUserId(Long id, Long userId);

    @Query(
            value = """
                    SELECT * FROM notes n
                    WHERE n.user_id = :userId
                      AND (
                        to_tsvector('english', coalesce(n.title, '')) @@ plainto_tsquery('english', :query)
                        OR to_tsvector('english', coalesce(n.content, '')) @@ plainto_tsquery('english', :query)
                      )
                    """,
            countQuery = """
                    SELECT count(*) FROM notes n
                    WHERE n.user_id = :userId
                      AND (
                        to_tsvector('english', coalesce(n.title, '')) @@ plainto_tsquery('english', :query)
                        OR to_tsvector('english', coalesce(n.content, '')) @@ plainto_tsquery('english', :query)
                      )
                    """,
            nativeQuery = true
    )
    Page<Note> searchNotes(@Param("userId") Long userId, @Param("query") String query, Pageable pageable);
}
