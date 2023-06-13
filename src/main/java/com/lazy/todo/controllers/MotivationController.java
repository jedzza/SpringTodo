package com.lazy.todo.controllers;


import com.lazy.todo.exceptions.AccessDeniedException;
import com.lazy.todo.exceptions.NoSuchTaskException;
import com.lazy.todo.models.Motivation;
import com.lazy.todo.payload.request.MotivationRequest;
import com.lazy.todo.payload.response.MessageResponse;
import com.lazy.todo.payload.response.MotivationResponse;
import com.lazy.todo.security.jwt.JwtUtils;
import com.lazy.todo.services.AccountService;
import com.lazy.todo.services.MotivationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

//this controller allows us to accesss OpenAPI to get motivation or congratulations for completing tasks
@RestController
@RequestMapping("/api/motivation")
public class MotivationController {

    @Qualifier("openaiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Autowired
    AccountService accountService;

    @Autowired
    MotivationService motivationService;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/congratulate")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> chatGptCongratulate(@RequestBody Motivation motivation) {
        // create a request

        String prompt = String.format(
                "give me a  motivational message to congratulate me on completing %s as if you are %s. it should be no longer than three sentences",
                motivation.getTitle(), motivation.getPersonality());
        MotivationRequest request = new MotivationRequest(model, prompt);
        // call the API
        MotivationResponse response = restTemplate.postForObject(apiUrl, request, MotivationResponse.class);

        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No response");
        }

        // return the first response
        return ResponseEntity.ok(response.getChoices().get(0).getMessage().getContent());
    }

    @GetMapping("/encourage/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> chatGptEncourage(@PathVariable("id") Long id, @RequestHeader(name = "Authorization") String token) {
        // create a request
        Motivation motivation = new Motivation();
        String jwt = token.substring(7);
        if (jwtUtils.validateJwtToken(jwt) && jwt != null)
        {
            try {
                motivation = motivationService.createMotivation(jwt, id);
            } catch (NoSuchTaskException e) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(e.getMessage());
            } catch (AccessDeniedException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            }
            String prompt = String.format(
                    "encourage me to complete %s as if you are %s. The response should be no longer than three sentences",
                    motivation.getTitle(), motivation.getPersonality());
            MotivationRequest request = new MotivationRequest(model, prompt);
            // call the API
            MotivationResponse response = restTemplate.postForObject(apiUrl, request, MotivationResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No response");
            }

            // return the first response
            return ResponseEntity.ok(response.getChoices().get(0).getMessage().getContent());
        }
        return ResponseEntity
                .badRequest().body(new MessageResponse("JWT authentication error"));
    }

    //Allow users to change their encouragement personality
    @PostMapping("/personality/{personality}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> changePersonality(@PathVariable("personality") String personality,
                                               @RequestHeader(name = "Authorization") String token) {
        String jwt = token.substring(7);
        try {
            return ResponseEntity.ok(accountService.changePersonality(jwt, personality));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No username found");
        }

        // return the first response

    }

}
