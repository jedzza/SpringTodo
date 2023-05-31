package com.lazy.todo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@NamedNativeQuery(name = "Project.sortedTasks",
        query = "SELECT t.id, t.title, t.description, t.checked, t.priority, t.project_priority," +
                " t.start_date, t.target_date\n" +
                "FROM defaultdb.user_tasks ut \n" +
                "\tINNER JOIN defaultdb.users u\n" +
                "\t\tON ut.user_id = u.id\n" +
                "\tINNER JOIN defaultdb.tasks t\n" +
                "\t\tON ut.task_id = t.id\n" +
                "WHERE (t.completed_on > (NOW() - INTERVAL 1 YEAR) OR t.checked IS NULL)\n" +
                "AND u.id = (?1)" +
                "AND t.project_id = (?2)\n" +
                "ORDER BY t.priority")

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
    private List<Task> tasks;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

}
