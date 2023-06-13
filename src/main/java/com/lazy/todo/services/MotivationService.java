package com.lazy.todo.services;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Motivation;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.repository.TaskRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MotivationService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    JwtUtils jwtUtils;

    public Motivation createMotivation(String jwt, Long id) throws NoSuchTaskException, AccessDeniedException {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchTaskException("Task Not Found with task id " + id));
        if (!user.getTasks().contains(task)){
            throw new AccessDeniedException("you do not have access to this task");
        }
        return new Motivation(task.getTitle(), user.getPersonality());
    }
}
