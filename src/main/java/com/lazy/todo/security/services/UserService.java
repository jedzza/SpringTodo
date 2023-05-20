package com.lazy.todo.security.services;

import com.lazy.todo.controllers.AuthController;
import com.lazy.todo.models.PasswordResetToken;
import com.lazy.todo.models.User;
import com.lazy.todo.payload.request.PasswordResetRequest;
import com.lazy.todo.repository.PasswordResetTokenRepository;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Calendar;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    AuthController authController;

    @Autowired
    JwtUtils jwtUtils;

    public PasswordResetToken createPasswordResetTokenForUser(PasswordResetRequest passwordResetRequest) {

        User user = userRepository.findByEmail(passwordResetRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email " + passwordResetRequest.getEmail()));
        String token = jwtUtils.generateResetToken(user);
        return passwordResetTokenRepository.save(new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(30)));
    }

    public String deleteUser(String jwt) throws  UsernameNotFoundException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));
        userRepository.delete(user);
        return username + " successfully deleted";
    }

}
