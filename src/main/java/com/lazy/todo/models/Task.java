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

@NamedNativeQuery(name = "Task.sortedTasks",
        query = "SELECT t.id, t.title, t.description, t.checked, t.priority, t.project_priority, t.project_id," +
                " t.start_date, t.target_date\n" +
                "FROM defaultdb.user_tasks ut \n" +
                "\tINNER JOIN defaultdb.users u\n" +
                "\t\tON ut.user_id = u.id\n" +
                "\tINNER JOIN defaultdb.tasks t\n" +
                "\t\tON ut.task_id = t.id\n" +
                "WHERE (t.completed_on > (NOW() - INTERVAL 1 YEAR) OR t.checked IS NULL)\n" +
                "\tAND u.id = ?1\n" +
                "\tAND t.project_id IS NULL " +
                "ORDER BY t.priority")

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

    private LocalDate completedOn;

    private Boolean checked;

    private int priority;

    private int projectPriority;


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
    public Task(LocalDate startDate, LocalDate targetDate, Boolean checked, String title, String description){
        this.startDate = startDate;
        this.targetDate = targetDate;
        this.checked = checked;
        this.title = title;
        this.description = description;
    }




}
