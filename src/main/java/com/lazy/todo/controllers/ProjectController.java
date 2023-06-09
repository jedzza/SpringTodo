package com.lazy.todo.controllers;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchProjectException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Project;
import com.lazy.todo.models.Task;
import com.lazy.todo.payload.request.PriorityUpdate;
import com.lazy.todo.payload.request.PriorityUpdateList;
import com.lazy.todo.payload.request.ProjectRequest;
import com.lazy.todo.payload.request.TaskRequest;
import com.lazy.todo.payload.response.MessageResponse;
import com.lazy.todo.repository.ProjectRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.ProjectService;
import com.lazy.todo.services.SortingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

//this class to controll all actions to do with projects - projects contain tasks
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    SortingService sortingService;

    @Autowired
    ProjectService projectService;

    //create a new project
    @PostMapping("/new")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> newProject(@RequestHeader(name = "Authorization") String token,
                                        @RequestBody ProjectRequest projectRequest) {
        //as elsewhere, trim "bearer:"
        String jwt = token.substring(7);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity
                    .ok(projectService.newProject(jwt, projectRequest));
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getProjectById(@PathVariable("id") Long id, @RequestHeader(name = "Authorization") String token)  {

        String jwt = token.substring(7);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            try {
                return ResponseEntity
                        .ok(projectService.getProjectById(jwt, id));
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            } catch (NoSuchProjectException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            }
        }
        return ResponseEntity
                .badRequest().body("malformed request");
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllProjects(@RequestHeader(name = "Authorization") String token) {

        String jwt = token.substring(7);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            return ResponseEntity
                    .ok(projectService.getAllProjectsByUser(jwt));
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }
    @GetMapping("/all/sorted")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getAllCurrentProjectTasks(@RequestHeader(name = "Authorization") String token) {

        String jwt = token.substring(7);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                return ResponseEntity.ok(sortingService.getSortedProjects(jwt));
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

    @PutMapping("/priority")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProjectPriority(@RequestHeader(name = "Authorization") String token,
                                                       @RequestBody PriorityUpdateList updateList) {
        String jwt = token.substring(7);
        List<Project> returnProjects = new ArrayList<>();
        if (jwtUtils.validateJwtToken(jwt) && jwt != null) {
            for (PriorityUpdate priorityUpdate: updateList.getPriorityUpdates())
            {
                try {
                    returnProjects.add(projectService.setProjectPriority(jwt, priorityUpdate.getId(), priorityUpdate.getPriority()));
                } catch (AccessDeniedException e) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
                } catch (NoSuchTaskException e) {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
                }
            }
            return ResponseEntity.ok(returnProjects);
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authetication error"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProjectById(@PathVariable("id") Long id,
                                    @RequestHeader(name = "Authorization") String token,
                                    @RequestBody ProjectRequest projectRequest)  {
        String jwt = token.substring(7);
        if (jwtUtils.validateJwtToken(jwt) && jwt != null) {
            try {
                return ResponseEntity
                        .ok(projectService.updateProjectById(jwt, id, projectRequest));
            } catch (NoSuchProjectException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            }
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProjectById (@PathVariable("id") Long id,
                                                @RequestHeader(name = "Authorization") String token)  {
        String jwt = token.substring(7);
        if (jwtUtils.validateJwtToken(jwt) && jwt != null) {
            try {
                return ResponseEntity
                        .ok(projectService.deleteProjectById(jwt, id));
            } catch (NoSuchProjectException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            } catch (NoSuchTaskException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            }
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

    /**
     * Accept a Taskrequest, add task to a project
     */
    @PutMapping("/addTask/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addTaskToProject(@PathVariable("id") Long id,
                                              @RequestHeader(name = "Authorization") String token,
                                              @RequestBody TaskRequest taskRequest){
        String jwt = token.substring(7);
        if (jwtUtils.validateJwtToken(jwt)) {
            try {
                return ResponseEntity.ok(projectService.addTaskToProject(jwt, id, taskRequest));
            } catch (NoSuchProjectException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            }
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

}