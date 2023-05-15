package com.lazy.todo.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


//this DTO matches the fields required in an OpenAPI request
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MotivationRequest {


    private String model;
    private List<Message> messages;
    private int n;
    private double temperature;

    public MotivationRequest(String model, String prompt) {
        this.model = model;
        this.n =1;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }
}
