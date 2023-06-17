package com.lazy.todo.services;

import com.lazy.todo.models.User;
import com.lazy.todo.repository.UserRepository;
import com.lazy.todo.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
public class AccountService {



    @PersistenceContext
    EntityManager entityManager;

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

    public String getPersonality(String jwt) throws  UsernameNotFoundException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));
        return user.getPersonality();
    }

    public User changePersonality(String jwt, String personality) throws  UsernameNotFoundException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username " + username));
        user.setPersonality(personality);
        return userRepository.save(user);
         }

    public List<Integer> getScore(String jwt) {
        String username =jwtUtils.getUserNameFromJwtToken(jwt);
        User user =userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username " + username));
        Query q = entityManager.createNamedQuery("User.scoreCount");
        q.setParameter(1, user.getId());
        return (List<Integer>) q.getResultList();
    }

}
