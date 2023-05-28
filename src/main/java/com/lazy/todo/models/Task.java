package com.lazy.todo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Size(max = 100)
    private String title;

    private String description;

    private LocalDate startDate;

    private LocalDate targetDate;

    private LocalDate checked;


    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Project project;

    @JsonIgnore
    @ManyToMany(mappedBy = "tasks")
    private Set<User> users = new HashSet<>();

    //a constructor for creating a task with no deadlines
    public Task(String title, String description) {
        this.title= title;
        this.description = description;
    }


    //define a constructor here to allow for creation of "headless" task without a project
    public Task(LocalDate startDate, LocalDate targetDate, LocalDate checked, String title, String description){
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.checked = checked;
        this.title = title;
        this.description = description;
    }




}
