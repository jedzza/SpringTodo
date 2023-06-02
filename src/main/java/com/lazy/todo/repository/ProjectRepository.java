package com.lazy.todo.repository;

import com.lazy.todo.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {


    @Transactional
    @Modifying
    @Query("delete from Task t where t.id = ?1")
    void delete(Long entityId);
}
