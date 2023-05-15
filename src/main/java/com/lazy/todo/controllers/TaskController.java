package com.lazy.todo.controllers;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Task;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.payload.response.MessageResponse;
import com.lazy.todo.repository.TaskRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    JwtUtils jwtUtils;


    @Autowired
    TaskService taskService;


    @PostMapping("/new")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> newTasks(@RequestHeader(name = "Authorization") String token,
                                      @RequestBody TaskRequest taskRequest) {
        String jwt = token.substring(7);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity.ok(taskService.saveNewTask(jwt, taskRequest));
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getTaskById(@PathVariable("id") Long id, @RequestHeader(name = "Authorization") String token) {

        String jwt = token.substring(7);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            try {
                Task task = taskService.getTaskById(jwt, id);
                if (task != null) {
                    return ResponseEntity
                            .ok(task);
                } else return ResponseEntity
                        .status(HttpStatus.NO_CONTENT).body("Task Not found with id " + id);
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            } catch (NoSuchTaskException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            }
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllTasks(@RequestHeader(name = "Authorization") String token) {

        String jwt = token.substring(7);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity
                    .ok(taskService.getAllTasks(jwt));
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getTaskUsers(@PathVariable("id") Long id, @RequestHeader(name = "Authorization") String token) {

        String jwt = token.substring(7);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            try {
                return ResponseEntity
                        .ok(taskService.getTaskUsers(jwt, id));
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            } catch (NoSuchTaskException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            }
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateTask(@PathVariable("id") Long id,
                                        @RequestHeader(name = "Authorization") String token,
                                        @RequestBody TaskRequest taskRequest) {
        String jwt = token.substring(7);
        if (jwtUtils.validateJwtToken(jwt) && jwt != null) {
            try {
                return ResponseEntity
                        .ok(taskService.updateTaskById(jwt, id, taskRequest));
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            } catch (NoSuchTaskException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            }
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authetication error"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteTask(@PathVariable("id") Long id,
                                         @RequestHeader(name = "Authorization") String token)  {

        String jwt = token.substring(7);
        if (jwtUtils.validateJwtToken(jwt) && jwt != null){
            try {
                return ResponseEntity.ok(taskService.deleteTaskById(jwt, id) + " deleted");
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            } catch (NoSuchTaskException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            }
        }
            return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authetication error"));
    }
}
