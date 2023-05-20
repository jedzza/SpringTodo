package com.lazy.todo.services;

import com.lazy.todo.models.User;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;


    public String deleteUser(String jwt) throws  UsernameNotFoundException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));
        userRepository.delete(user);
        return username + " successfully deleted";
    }
}
