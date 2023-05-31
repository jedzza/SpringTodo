package com.lazy.todo.services;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchProjectException;
import com.lazy.todo.models.Project;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.repository.ProjectRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SortingService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;


    public List<Task> getCurrentTasks(String jwt) {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        Query q = entityManager.createNamedQuery("Task.sortedTasks");
        q.setParameter(1, user.getId());
        return q.getResultList();
    }

    public List<Task> getCurrentProjectTasks(String jwt, Long projectId) throws NoSuchProjectException, AccessDeniedException {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchProjectException("No project with Id " + projectId));
        if (!user.getProjects().contains(project)){
            throw new AccessDeniedException("You don't have access to this project");
        }
        Query q = entityManager.createNamedQuery("Project.sortedTasks");
        q.setParameter(1, user.getId());
        q.setParameter(2, projectId);
        return q.getResultList();
    }
}