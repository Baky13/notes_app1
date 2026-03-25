package com.example.notesapp.repository;

import com.example.notesapp.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByColumnIdOrderByPosition(Long columnId);
    Optional<Task> findByIdAndColumnId(Long id, Long columnId);
}
