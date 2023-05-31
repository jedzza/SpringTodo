package com.lazy.todo.services;

import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
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


    public List<Task> getCurrentTasks(String jwt) {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        Query q = entityManager.createNamedQuery("Task.sortedTasks");
        q.setParameter(1, user.getId());
        return q.getResultList();
    }
}