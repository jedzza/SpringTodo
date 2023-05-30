package com.lazy.todo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "projects")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 100)
    private String title;

    private String description;

    private LocalDate startLocalDate;

    private LocalDate targetLocalDate;

    private LocalDate checked;

    private int project;

    public Project(String title, String description) {
        this.title= title;
        this.description =description;
    }

    @OneToMany(mappedBy = "project")
    private Set<Task> tasks;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

}
