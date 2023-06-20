package com.lazy.todo.controllers;

import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
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

    @Autowired
    JwtUtils jwtUtils;

    @DeleteMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteAccount (@RequestHeader(name = "Authorization") String token) {
        //remove "bearer:" from the authorization token, leaving just the actual token
        String jwt = token.substring(7);
        if (jwtUtils.validateJwtToken(jwt) && jwt != null) {
            try {
                return ResponseEntity.ok(userService.deleteUser(jwt));
            } catch (UsernameNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JWT authentication error");
    }
    @PutMapping("/email/{email}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> changeEmail(@RequestHeader(name = "Authorization") String token,
                                         @PathVariable(name = "email") String email) {
        String jwt = token.substring(7);
        if (jwtUtils.validateJwtToken(jwt) && jwt != null) {
            try {
                return ResponseEntity.ok(userService.updateEmail(jwt, email));
            } catch (UsernameNotFoundException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("JWT authentication error");
    }

    @GetMapping("/loggedin")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public  ResponseEntity<?> loggedInCheck(@RequestHeader(name = "Authorization") String token,
                                            @PathVariable(name = "email") String email) {
        return ResponseEntity.ok("you're logged in");
    }

}
