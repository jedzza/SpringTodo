package com.lazy.todo.services;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Task;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.repository.TaskRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {



    @Autowired
    TaskRepository taskRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    public Task saveNewTask(String jwt, TaskRequest task) {
            String username = jwtUtils.getUserNameFromJwtToken(jwt);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
            Task newTask = new Task();
            BeanUtils.copyProperties(task, newTask, "id");
            user.getTasks().add(newTask);
            return taskRepository.save(newTask);
        }

    public Task getTaskById(String jwt, Long id) throws NoSuchTaskException, AccessDeniedException {

        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchTaskException("Task Not Found with task id " + id));
        if (!user.getTasks().contains(task)){
            throw new AccessDeniedException();
        }
        return task;
        }

    public Set<Task> getAllTasks(String jwt) {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        return user.getTasks();
    }

    public Task completeTask(String jwt, Long id) throws NoSuchTaskException, AccessDeniedException {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchTaskException("Task Not Found with task id " + id));
        if (!user.getTasks().contains(task)) {
            throw new AccessDeniedException("you do not have access to this task");
        }
        task.setChecked(LocalDate.now());
        return taskRepository.save(task);
    }

    public Task unCompleteTask(String jwt, Long id) throws NoSuchTaskException, AccessDeniedException {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchTaskException("Task Not Found with task id " + id));
        if (!user.getTasks().contains(task)) {
            throw new AccessDeniedException("you do not have access to this task");
        }
        task.setChecked(null);
        return taskRepository.save(task);
    }


    public Set<String> getTaskUsers(String jwt, Long id) throws AccessDeniedException, NoSuchTaskException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchTaskException("Task Not Found With ID " + id));
        if (!currentUser.getTasks().contains(task)) {
            throw new AccessDeniedException("you do not have access to this task");
        }
        Set<String> usernames = new HashSet<>();
        for (User returnedUser: task.getUsers()){
            usernames.add(returnedUser.getUsername());
        }
        return usernames;
    }

    public Task updateTaskById(String jwt, Long id, TaskRequest taskRequest) throws NoSuchTaskException, AccessDeniedException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with userame: " + username));
        Task updatedTask = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchTaskException("Task Not Found with task id " + id));
        if (!user.getTasks().contains(updatedTask)) {
            throw new AccessDeniedException("you do not have access to this task");
        }
        BeanUtils.copyProperties(taskRequest, updatedTask, "id", "project", "users");
        taskRepository.save(updatedTask);
        userRepository.save(user);
        return updatedTask;
    }

    public Task deleteTaskById(String jwt, Long id) throws NoSuchTaskException, AccessDeniedException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with userame: " + username));
        Task deletedTask = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchTaskException("Task Not Found with task id " + id));
        if (!user.getTasks().contains(deletedTask)) {
            throw new AccessDeniedException("You don't have access to that task");
        }
        for (User oldUser: deletedTask.getUsers()){
            oldUser.getTasks().remove(deletedTask);
            userRepository.save(oldUser);
        }
        taskRepository.delete(id);
        return deletedTask;
    }
}
