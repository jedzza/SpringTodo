package com.lazy.todo.controllers;

import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchProjectException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.payload.request.ProjectRequest;
import com.lazy.todo.payload.response.MessageResponse;
import com.lazy.todo.repository.ProjectRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/project")
public class ProjectController {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ProjectService projectService;

    @PostMapping("/new")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> newProject(@RequestHeader(name = "Authorization") String token,
                                        @RequestBody ProjectRequest projectRequest) {
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
                .badRequest().body(new MessageResponse("JWT authetication error"));
    }

}