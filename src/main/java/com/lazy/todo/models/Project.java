package com.lazy.todo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Date;
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

    @NotBlank
    @Size(max = 100)
    private String title;

    private String description;

    private Date startDate;

    private Date targetDate;

    private Date checked;

    public Project(String title, String description) {
        this.title= title;
        this.description =description;
    }

    @OneToMany(mappedBy = "project")
    private Set<Task> tasks;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
