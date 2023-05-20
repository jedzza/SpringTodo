package com.lazy.todo.controllers;


import com.lazy.todo.models.Motivation;
import com.lazy.todo.payload.request.MotivationRequest;
import com.lazy.todo.payload.response.MotivationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

    @GetMapping("/congratulate")
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

    @GetMapping("/encourage")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> chatGptEncourage(@RequestBody Motivation motivation) {
        // create a request
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

}
