package com.lazy.todo.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lazy.todo.models.gson.LocalDateAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

//this allows us to adapt LocalDates to and from JSON for serialization, here we create the relevant bean
@Configuration
public class GsonConfig {

    @Bean
    public Gson gson() {
    return new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();
    }

}