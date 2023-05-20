package com.lazy.todo.controllers;

import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    UserService userService;

    @DeleteMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAccount (@RequestHeader(name = "Authorization") String token) {
        String jwt = token.substring(7);
        try {
            return ResponseEntity.ok(userService.deleteUser(jwt));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
        }
    }
}
