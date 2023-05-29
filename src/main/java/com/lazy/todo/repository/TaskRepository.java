package com.lazy.todo.repository;

import com.lazy.todo.models.ERole;
import com.lazy.todo.models.Role;
import com.lazy.todo.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findById(Task task);

    @Transactional
    @Modifying
    @Query("delete from Task t where t.id = ?1")
    void delete(Long entityId);

}